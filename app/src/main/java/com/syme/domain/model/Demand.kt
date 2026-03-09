package com.syme.domain.model

import com.syme.domain.model.enumeration.DemandStatus

/**
 * Represents a voluntary power reduction request made by a client.
 * A demand replaces the subscription period for its duration.
 * It may span across two billing periods and must be split accordingly.
 */
data class Demand(
    val demandId: String = "",
    val installationId: String = "",
    val ownerId: String = "",

    // Requested power during the demand period (kW) — must be <= powerSubscribed
    val requestedPowerKw: Double = 0.0,

    // Absolute start and end of the demand (epoch ms)
    val startTime: Long = 0L,
    val endTime: Long = 0L,

    val status: DemandStatus = DemandStatus.PENDING,

    val metadata: Map<String, Any>? = null,
    val trace: Traceability = Traceability()
) {
    /** Duration of this demand in milliseconds */
    val durationMs: Long get() = maxOf(0L, endTime - startTime)

    /** Duration of this demand in hours */
    val durationHours: Double get() = durationMs / 3_600_000.0

    /**
     * Returns the portion of this demand that falls within [periodStart, periodEnd].
     * Used to split a demand that spans two billing periods.
     */
    fun clampToPeriod(periodStart: Long, periodEnd: Long): Demand? {
        val clampedStart = maxOf(startTime, periodStart)
        val clampedEnd = minOf(endTime, periodEnd)
        if (clampedStart >= clampedEnd) return null
        return copy(startTime = clampedStart, endTime = clampedEnd)
    }
}
