package com.syme.domain.model

import com.syme.domain.model.enumeration.InvoiceStatus
import java.util.UUID

/**
 * Domain service responsible for computing an [Invoice] from raw inputs.
 *
 * Usage:
 * ```kotlin
 * val invoice = InvoiceCalculator.compute(
 *     installation   = installation,
 *     billingPeriod  = period,
 *     demands        = demands,        // all demands, may span outside the period
 *     totalEnergyKwh = 12_450.0,
 *     tariff         = tariffConfig
 * )
 * ```
 */
object InvoiceCalculator {

    fun compute(
        installation: Installation,
        billingPeriod: BillingPeriod,
        demands: List<Consumption>,
        totalEnergyKwh: Double,
        tariff: TariffConfig
    ): Invoice {

        // 1. Clamp demands to the current billing period
        val demandLines = demands
            .mapNotNull { demand ->
                demand.clampToPeriod(billingPeriod.periodStart, billingPeriod.periodEnd)
                    ?.let { clamped ->
                        InvoiceDemandLine(
                            demandId       = demand.consumptionId,
                            effectiveStart = clamped.periodStart,
                            effectiveEnd   = clamped.periodEnd,
                            durationHours  = clamped.durationHours,
                            requestedPowerKw = clamped.requestedPowerKw,
                            cost           = clamped.durationHours * clamped.requestedPowerKw * tariff.pricePerKwDemand
                        )
                    }
            }

        // 2. Time breakdown
        val totalHours        = billingPeriod.totalHours
        val totalDemandHours  = demandLines.sumOf { it.durationHours }
        val subscriptionHours = totalHours - totalDemandHours

        // 3. Cost components
        val subscriptionCost = subscriptionHours * installation.powerSubscribed * tariff.pricePerKwSubscription
        val demandCost       = demandLines.sumOf { it.cost }
        val energyCost       = totalEnergyKwh * tariff.pricePerKwh

        val subTotal = subscriptionCost + demandCost + energyCost

        // 4. Adjustments
        val vatAmount            = subTotal * tariff.vatRate
        val otherTaxesAmount     = subTotal * tariff.otherTaxesRate
        val bonusAmount          = subTotal * tariff.bonusRate
        val socialDiscountAmount = subTotal * tariff.socialDiscountRate
        val networkBalancingAmount = subTotal * (tariff.networkBalancingFactor - 1.0)

        // Facture = subTotal × networkBalancingFactor × (1 + netAdjustmentRate)
        val totalAmount = subTotal * tariff.networkBalancingFactor * (1.0 + tariff.netAdjustmentRate)

        return Invoice(
            invoiceId             = UUID.randomUUID().toString(),
            installationId        = installation.installationId,
            ownerId               = installation.ownerId,
            billingPeriod         = billingPeriod,
            totalHours            = totalHours,
            totalDemandHours      = totalDemandHours,
            totalSubscriptionHours = subscriptionHours,
            subscribedPowerKw     = installation.powerSubscribed,
            totalEnergyKwh        = totalEnergyKwh,
            subscriptionCost      = subscriptionCost,
            demandCost            = demandCost,
            energyCost            = energyCost,
            subTotal              = subTotal,
            vatAmount             = vatAmount,
            otherTaxesAmount      = otherTaxesAmount,
            bonusAmount           = bonusAmount,
            socialDiscountAmount  = socialDiscountAmount,
            networkBalancingAmount = networkBalancingAmount,
            totalAmount           = totalAmount,
            tariffConfig          = tariff,
            demandLines           = demandLines,
            status                = InvoiceStatus.DRAFT
        )
    }
}
