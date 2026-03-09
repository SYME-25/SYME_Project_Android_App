package com.syme.domain.model

/**
 * Optional per-installation tariff overrides set by the SYME admin.
 *
 * Only non-null fields override the global [TariffConfig] for this installation.
 * All fields are null by default — meaning "use the global tariff".
 *
 * Stored at:
 *   users/{ownerId}/installations/{installationId}/tariff/override
 *
 * Example: a negotiated client gets a lower [pricePerKwh] but inherits
 * everything else from the global residential tariff.
 *
 * Read-only from the client app — only the SYME admin can write this document.
 */
data class TariffOverride(
    val pricePerKwSubscription : Double? = null,
    val pricePerKwDemand       : Double? = null,
    val pricePerKwh            : Double? = null,
    val penaltyPricePerKwh     : Double? = null,
    val vatRate                : Double? = null,
    val otherTaxesRate         : Double? = null,
    val bonusRate              : Double? = null,
    val socialDiscountRate     : Double? = null,
    val networkBalancingFactor : Double? = null
) {
    /**
     * Merges this override onto a [base] [TariffConfig].
     * Each non-null field in this override replaces the corresponding field in [base].
     */
    fun applyTo(base: TariffConfig): TariffConfig = base.copy(
        pricePerKwSubscription  = pricePerKwSubscription  ?: base.pricePerKwSubscription,
        pricePerKwDemand        = pricePerKwDemand        ?: base.pricePerKwDemand,
        pricePerKwh             = pricePerKwh             ?: base.pricePerKwh,
        penaltyPricePerKwh      = penaltyPricePerKwh      ?: base.penaltyPricePerKwh,
        vatRate                 = vatRate                 ?: base.vatRate,
        otherTaxesRate          = otherTaxesRate          ?: base.otherTaxesRate,
        bonusRate               = bonusRate               ?: base.bonusRate,
        socialDiscountRate      = socialDiscountRate      ?: base.socialDiscountRate,
        networkBalancingFactor  = networkBalancingFactor  ?: base.networkBalancingFactor
    )
}
