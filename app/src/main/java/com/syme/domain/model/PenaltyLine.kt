package com.syme.domain.model

/**
 * Represents a gap in subscription/demand coverage within a billing period.
 *
 * A penalty line is created for every time interval inside the billing period
 * that is NOT covered by any [Consumption] (subscription or demand).
 * The client is billed at [TariffConfig.penaltyPricePerKwh] for the energy
 * estimated to have been consumed during that gap.
 *
 * Energy estimation:
 *   gapEnergyKwh = (gapDurationHours / totalPeriodHours) × totalEnergyKwh
 *
 * Cost:
 *   cost = gapEnergyKwh × penaltyPricePerKwh
 */
data class PenaltyLine(
    /** Absolute start of the uncovered gap (epoch ms) */
    val gapStart: Long = 0L,
    /** Absolute end of the uncovered gap (epoch ms) */
    val gapEnd: Long = 0L,
    /** Duration of this gap in hours */
    val durationHours: Double = 0.0,
    /** Energy estimated to have been consumed during this gap (kWh) */
    val energyKwh: Double = 0.0,
    /** Penalty cost for this gap */
    val cost: Double = 0.0
)
