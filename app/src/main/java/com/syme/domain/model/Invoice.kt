package com.syme.domain.model

import com.syme.domain.model.enumeration.InvoiceStatus

/**
 * Represents a computed invoice for one billing period.
 *
 * Billing formula:
 *   subscriptionCost  = T_subscription  × P_sub  × pricePerKwSubscription
 *   demandCost        = Σ(duration_i    × P_dem_i × pricePerKwDemand)
 *   energyCost        = totalEnergyKwh  × pricePerKwh
 *   penaltyCost       = Σ(gap_i_energyKwh × penaltyPricePerKwh)
 *
 *   subTotal          = subscriptionCost + demandCost + energyCost + penaltyCost
 *   totalAmount       = subTotal × networkBalancingFactor × (1 + netAdjustmentRate)
 */
data class Invoice(
    val invoiceId: String = "",
    val installationId: String = "",
    val ownerId: String = "",

    // Billing period reference
    val billingPeriod: BillingPeriod = BillingPeriod(),

    // ── Time breakdown (hours) ────────────────────────────────────────────────
    /** Total hours in the billing period */
    val totalHours: Double = 0.0,
    /** Hours spent in demand mode (voluntary reduction) */
    val totalDemandHours: Double = 0.0,
    /** totalHours − totalDemandHours − totalPenaltyHours */
    val totalSubscriptionHours: Double = 0.0,
    /** Hours not covered by any subscription or demand (out-of-contract gaps) */
    val totalPenaltyHours: Double = 0.0,

    // ── Power & energy inputs ─────────────────────────────────────────────────
    /** Contracted subscription power (kW) */
    val subscribedPowerKw: Double = 0.0,
    /** Total energy actually consumed during the period (kWh) */
    val totalEnergyKwh: Double = 0.0,

    // ── Cost breakdown ────────────────────────────────────────────────────────
    val subscriptionCost: Double = 0.0,
    val demandCost: Double = 0.0,
    val energyCost: Double = 0.0,
    /** Penalty cost for energy consumed outside any valid covered period */
    val penaltyCost: Double = 0.0,
    /** subscriptionCost + demandCost + energyCost + penaltyCost */
    val subTotal: Double = 0.0,

    // ── Adjustments ───────────────────────────────────────────────────────────
    val vatAmount: Double = 0.0,
    val otherTaxesAmount: Double = 0.0,
    val bonusAmount: Double = 0.0,
    val socialDiscountAmount: Double = 0.0,
    val networkBalancingAmount: Double = 0.0,
    /** Final amount due */
    val totalAmount: Double = 0.0,

    // ── Snapshot of tariff used ───────────────────────────────────────────────
    val tariffConfig: TariffConfig = TariffConfig(),

    // ── Demand lines that contributed to this invoice ─────────────────────────
    /** Each entry is the portion of a demand clamped to this billing period */
    val demandLines: List<InvoiceDemandLine> = emptyList(),

    // ── Penalty lines (gaps in subscription/demand coverage) ──────────────────
    /** Each entry is an uncovered time gap that triggered a penalty */
    val penaltyLines: List<PenaltyLine> = emptyList(),

    val status: InvoiceStatus = InvoiceStatus.DRAFT,
    val issuedAt: Long? = null,
    val dueDate: Long? = null,
    val metadata: Map<String, Any>? = null,
    val trace: Traceability = Traceability()
)
