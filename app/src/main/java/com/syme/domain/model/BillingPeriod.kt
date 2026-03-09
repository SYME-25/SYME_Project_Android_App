package com.syme.domain.model

/**
 * Represents a fixed 2-month billing period for a given installation.
 */
data class BillingPeriod(
    val billingPeriodId: String = "",
    val installationId: String = "",

    // Epoch ms boundaries of this billing period
    val periodStart: Long = 0L,
    val periodEnd: Long = 0L,
) {
    /** Total duration of the billing period in hours */
    val totalHours: Double get() = (periodEnd - periodStart) / 3_600_000.0
}
