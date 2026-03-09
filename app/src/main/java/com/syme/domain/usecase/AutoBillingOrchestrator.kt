package com.syme.domain.usecase

import com.syme.data.remote.repository.ConsumptionRepository
import com.syme.data.remote.repository.InvoiceRepository
import com.syme.data.remote.repository.MeterRepository
import com.syme.data.remote.repository.TariffRepository
import com.syme.domain.model.BillingEngine
import com.syme.domain.model.BillingPeriod
import com.syme.domain.model.Consumption
import com.syme.domain.model.Installation
import com.syme.domain.model.enumeration.InstallationType
import com.syme.ui.screen.bill.services.BillingService
import javax.inject.Inject

/**
 * Use case responsible for automatically computing and persisting the current invoice
 * each time a new aggregated measurement is received.
 *
 * ## Tariff resolution
 * Resolved from two layers via [TariffRepository.getOnce]:
 *   1. Global tariff for the installation's [InstallationType]  ← tariffs/{type}/config/current
 *   2. Optional per-installation override (admin-set only)      ← .../tariff/override
 *
 * ## Period resolution
 *   - Active sub  → period = [sub.periodStart … now], consumptions = all overlapping
 *   - Expired sub → period = [lastSub.periodEnd … now], consumptions = empty (full penalty)
 *   - No sub      → returns early, nothing to bill
 *
 * ## Auto-archive
 * If the "current" invoice belongs to a different subscription period,
 * it is archived before the new one is written.
 */
class AutoBillingOrchestrator @Inject constructor(
    private val consumptionRepository: ConsumptionRepository,
    private val invoiceRepository: InvoiceRepository,
    private val tariffRepository: TariffRepository,
    private val meterRepository: MeterRepository,
    private val billingService: BillingService
) {
    suspend fun run(ownerId: String, installation: Installation) {
        val installationId   = installation.installationId
        val installationType = installation.type
        val now              = System.currentTimeMillis()

        // 1. Resolve tariff (global + optional override)
        val tariff = tariffRepository.getOnce(
            ownerId          = ownerId,
            installationId   = installationId,
            installationType = installationType
        ) ?: return

        // 2. Load consumptions
        val consumptions = consumptionRepository.getAllOnce(ownerId, installationId)

        // 3. Resolve billing period
        val (billingPeriod, activeConsumptions) = resolvePeriod(consumptions, now)
            ?: return

        // 4. Archive if subscription period changed
        val existingCurrent = invoiceRepository.getCurrentOnce(ownerId, installationId)
        if (existingCurrent != null &&
            existingCurrent.billingPeriod.periodStart != billingPeriod.periodStart
        ) {
            invoiceRepository.archiveCurrent(ownerId, installationId)
        }

        billingService.generateInvoice(
            ownerId = ownerId,
            installation = installation,
            period = billingPeriod,
            tariff = tariff
        )
    }

    private fun resolvePeriod(
        consumptions: List<Consumption>,
        now: Long
    ): Pair<BillingPeriod, List<Consumption>>? {
        val subscriptions = consumptions
            .filter { !it.onDemand }
            .sortedBy { it.periodStart }

        if (subscriptions.isEmpty()) return null

        val activeSub = subscriptions.firstOrNull { it.periodEnd > now }

        return if (activeSub != null) {
            val period = BillingPeriod(periodStart = activeSub.periodStart, periodEnd = now)
            val relevant = consumptions.filter { it.periodStart < now && it.periodEnd > activeSub.periodStart }
            period to relevant
        } else {
            val lastSub = subscriptions.last()
            BillingPeriod(periodStart = lastSub.periodEnd, periodEnd = now) to emptyList()
        }
    }
}
