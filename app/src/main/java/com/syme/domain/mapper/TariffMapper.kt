package com.syme.domain.mapper

import com.syme.domain.model.TariffConfig

fun Map<String, Any>.toTariffConfig(): TariffConfig =
    TariffConfig(
        pricePerKwSubscription  = this["pricePerKwSubscription"] as? Double ?: 0.0,
        pricePerKwDemand        = this["pricePerKwDemand"] as? Double ?: 0.0,
        pricePerKwh             = this["pricePerKwh"] as? Double ?: 0.0,
        penaltyPricePerKwh      = this["penaltyPricePerKwh"] as? Double ?: 0.0,
        vatRate                 = this["vatRate"] as? Double ?: 0.0,
        otherTaxesRate          = this["otherTaxesRate"] as? Double ?: 0.0,
        bonusRate               = this["bonusRate"] as? Double ?: 0.0,
        socialDiscountRate      = this["socialDiscountRate"] as? Double ?: 0.0,
        networkBalancingFactor  = this["networkBalancingFactor"] as? Double ?: 1.0
    )