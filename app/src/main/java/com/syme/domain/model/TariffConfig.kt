package com.syme.domain.model

import com.syme.domain.model.enumeration.InstallationType

/**
 * Holds the pricing configuration used to compute a bill.
 * All prices are expected in the local currency unit (FCFA).
 *
 * ## Tariff resolution (handled by [TariffRepository])
 *
 *   1. Read global tariff : `tariffs/{installationType.firestoreKey}/config`
 *   2. Read override      : `users/{uid}/installations/{id}/tariff/override`
 *   3. Merge             : non-null override fields replace global values
 *
 * [installationType] is stored as a snapshot in the invoice for traceability —
 * so an archived invoice always knows which tariff profile was applied.
 *
 * @param installationType       Type of installation this tariff applies to (snapshot)
 * @param pricePerKwSubscription Price per kW·h during subscription periods
 * @param pricePerKwDemand       Price per kW·h during demand periods (< subscription)
 * @param pricePerKwh            Price per kWh of energy actually consumed
 * @param penaltyPricePerKwh     Price per kWh for energy consumed out-of-contract
 * @param vatRate                VAT rate as a decimal (e.g. 0.18 for 18 %)
 * @param otherTaxesRate         Other applicable taxes as a decimal
 * @param bonusRate              Bonus rate for voluntary demand reduction
 * @param socialDiscountRate     Social discount rate
 * @param networkBalancingFactor Grid stabilisation factor (1.0 = no adjustment)
 */
data class TariffConfig(
    /** Snapshot of the installation type — for invoice traceability only */
    val installationType       : InstallationType? = null,
    val pricePerKwSubscription : Double = 0.0,
    val pricePerKwDemand       : Double = 0.0,
    val pricePerKwh            : Double = 0.0,
    val penaltyPricePerKwh     : Double = 0.0,
    // ── Adjustments ───────────────────────────────────────────────────────────
    val vatRate                : Double = 0.0,
    val otherTaxesRate         : Double = 0.0,
    val bonusRate              : Double = 0.0,
    val socialDiscountRate     : Double = 0.0,
    val networkBalancingFactor : Double = 1.0
) {
    /** Combined additive adjustment rate: taxes − bonuses − discounts */
    val netAdjustmentRate: Double
        get() = vatRate + otherTaxesRate - bonusRate - socialDiscountRate
}
