package com.syme.domain.model


/**
 * One line in the invoice representing a (possibly partial) demand period.
 * A demand that spans two billing periods produces one line per invoice.
 */
data class InvoiceDemandLine(
    val demandId: String = "",
    /** Effective start within this billing period (epoch ms) */
    val effectiveStart: Long = 0L,
    /** Effective end within this billing period (epoch ms) */
    val effectiveEnd: Long = 0L,
    /** Duration in hours for this period slice */
    val durationHours: Double = 0.0,
    /** Requested power during this demand (kW) */
    val requestedPowerKw: Double? = 0.0,
    /** Cost = durationHours × requestedPowerKw × pricePerKwDemand */
    val cost: Double = 0.0
)
