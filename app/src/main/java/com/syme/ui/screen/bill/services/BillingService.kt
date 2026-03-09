package com.syme.ui.screen.bill.services

import com.syme.data.remote.repository.ConsumptionRepository
import com.syme.data.remote.repository.InvoiceRepository
import com.syme.domain.model.BillingPeriod
import com.syme.domain.model.BillingEngine
import com.syme.domain.model.Installation
import com.syme.domain.model.TariffConfig
import javax.inject.Inject

/**
 * Application service responsible for generating invoices.
 *
 * Delegates all computation to [BillingEngine], which:
 *  - detects gaps in subscription/demand coverage → penalty lines
 *  - delegates covered-period arithmetic to [InvoiceCalculator]
 *  - merges both into the final [Invoice]
 */
class BillingService @Inject constructor(
    private val consumptionRepository: ConsumptionRepository,
    private val invoiceRepository: InvoiceRepository
) {
    suspend fun generateInvoice(
        ownerId: String,
        installation: Installation,
        period: BillingPeriod,
        tariff: TariffConfig
    ) {
        val installationId = installation.installationId

        val consumptions = consumptionRepository.getAllOnce(ownerId, installationId)

        val totalEnergyKwh = consumptions.sumOf { it.totalEnergy_kWhConsummed }

        val invoice = BillingEngine.compute(
            installation   = installation,
            billingPeriod  = period,
            consumptions   = consumptions,
            totalEnergyKwh = totalEnergyKwh,
            tariff         = tariff
        )

        invoiceRepository.upsertCurrent(
            ownerId = ownerId,
            installationId = installationId,
            invoice = invoice
        )
    }
}