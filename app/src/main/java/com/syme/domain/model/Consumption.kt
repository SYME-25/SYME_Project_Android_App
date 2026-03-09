package com.syme.domain.model

import com.syme.domain.model.enumeration.ConsumptionStateType

/**
 * Represents either a planned subscription period or a voluntary demand (power reduction).
 *
 * - onDemand = false → standard subscription period (full contracted power)
 * - onDemand = true  → voluntary demand: client reduces power to [requestedPowerKw]
 *
 * A consumption may span two billing periods and will be clamped accordingly
 * during invoice computation.
 */
data class Consumption(
    val consumptionId: String = "",
    val installationId: String? = null,
    val meterId: String? = null,

    val periodStart: Long = 0L,                      // Start of period (epoch ms)
    val periodEnd: Long = 0L,                        // End of period (epoch ms)

    val totalEnergy_kWh: Int = 0,                    // Planned energy (kWh)
    val totalEnergy_kWhConsummed: Double = 0.0,      // Actual energy consumed (kWh)

    val consumptionState: ConsumptionStateType = ConsumptionStateType.WAITING,

    // ── Demand fields (only relevant when onDemand = true) ───────────────────
    /** True if this period is a voluntary power reduction (demand). */
    val onDemand: Boolean = false,

    /**
     * Requested power during this period (kW).
     * Must be <= installation.powerSubscribed.
     * Null when onDemand = false.
     */
    val requestedPowerKw: Double = 0.0,

    val metadata: Map<String, Any>? = null,
    val trace: Traceability = Traceability()
) {
    /** Duration in milliseconds */
    val durationMs: Long get() = maxOf(0L, periodEnd - periodStart)

    /** Duration in hours */
    val durationHours: Double get() = durationMs / 3_600_000.0

    /**
     * Returns the slice of this consumption that falls within [start, end].
     * Used to split a consumption that spans two billing periods.
     * Returns null if there is no overlap.
     */
    fun clampToPeriod(start: Long, end: Long): Consumption? {
        val clampedStart = maxOf(periodStart, start)
        val clampedEnd = minOf(periodEnd, end)
        if (clampedStart >= clampedEnd) return null
        return copy(periodStart = clampedStart, periodEnd = clampedEnd)
    }
}
