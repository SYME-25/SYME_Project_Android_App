package com.syme.domain.model

/**
 * Orchestrates invoice computation by:
 *  1. Detecting uncovered time gaps within the billing period (penalty detection)
 *  2. Delegating covered-period computation to [InvoiceCalculator]
 *  3. Merging penalty lines and penalty cost into the final [Invoice]
 *
 * A gap is any interval inside [BillingPeriod] that is NOT covered by any
 * [Consumption] — whether it is a subscription period (onDemand=false)
 * or a demand period (onDemand=true).
 *
 * Gap energy estimation:
 *   gapEnergyKwh = (gapDurationHours / totalPeriodHours) × totalEnergyKwh
 *
 * Penalty cost:
 *   penaltyCost = Σ gapEnergyKwh_i × tariff.penaltyPricePerKwh
 */
object BillingEngine {

    /**
     * Computes a full [Invoice] for the given [billingPeriod], including penalties
     * for any uncovered gaps.
     *
     * @param installation   The installation being billed
     * @param billingPeriod  The period to bill for
     * @param consumptions   All consumptions for this installation (any date range).
     *                       Will be clamped to the billing period internally.
     * @param totalEnergyKwh Total energy actually consumed during the billing period (kWh)
     * @param tariff         Tariff configuration including [TariffConfig.penaltyPricePerKwh]
     */
    fun compute(
        installation: Installation,
        billingPeriod: BillingPeriod,
        consumptions: List<Consumption>,
        totalEnergyKwh: Double,
        tariff: TariffConfig
    ): Invoice {
        val periodStart = billingPeriod.periodStart
        val periodEnd   = billingPeriod.periodEnd
        val totalHours  = billingPeriod.totalHours

        // ── 1. Clamp consumptions to the billing period ───────────────────────
        val coveredIntervals = consumptions
            .mapNotNull { it.clampToPeriod(periodStart, periodEnd) }
            .map { it.periodStart to it.periodEnd }

        // ── 2. Merge overlapping/adjacent intervals ───────────────────────────
        val mergedIntervals = mergeIntervals(coveredIntervals)

        // ── 3. Detect gaps ────────────────────────────────────────────────────
        val penaltyLines = buildPenaltyLines(
            periodStart    = periodStart,
            periodEnd      = periodEnd,
            totalHours     = totalHours,
            totalEnergyKwh = totalEnergyKwh,
            covered        = mergedIntervals,
            penaltyPrice   = tariff.penaltyPricePerKwh
        )

        val penaltyCost       = penaltyLines.sumOf { it.cost }
        val totalPenaltyHours = penaltyLines.sumOf { it.durationHours }

        // ── 4. Delegate covered-period computation to InvoiceCalculator ───────
        val baseInvoice = InvoiceCalculator.compute(
            installation   = installation,
            billingPeriod  = billingPeriod,
            demands        = consumptions,
            totalEnergyKwh = totalEnergyKwh,
            tariff         = tariff
        )

        // ── 5. Merge penalty into the invoice ─────────────────────────────────
        val subTotal = baseInvoice.subTotal + penaltyCost

        val vatAmount              = subTotal * tariff.vatRate
        val otherTaxesAmount       = subTotal * tariff.otherTaxesRate
        val bonusAmount            = subTotal * tariff.bonusRate
        val socialDiscountAmount   = subTotal * tariff.socialDiscountRate
        val networkBalancingAmount = subTotal * (tariff.networkBalancingFactor - 1.0)
        val totalAmount            = subTotal * tariff.networkBalancingFactor *
                                     (1.0 + tariff.netAdjustmentRate)

        return baseInvoice.copy(
            totalPenaltyHours      = totalPenaltyHours,
            penaltyCost            = penaltyCost,
            penaltyLines           = penaltyLines,
            subTotal               = subTotal,
            vatAmount              = vatAmount,
            otherTaxesAmount       = otherTaxesAmount,
            bonusAmount            = bonusAmount,
            socialDiscountAmount   = socialDiscountAmount,
            networkBalancingAmount = networkBalancingAmount,
            totalAmount            = totalAmount
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Internal helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Merges a list of possibly overlapping or adjacent [start, end] intervals
     * into a sorted, non-overlapping list.
     */
    private fun mergeIntervals(intervals: List<Pair<Long, Long>>): List<Pair<Long, Long>> {
        if (intervals.isEmpty()) return emptyList()

        val sorted = intervals.sortedBy { it.first }
        val merged = mutableListOf(sorted.first())

        for ((start, end) in sorted.drop(1)) {
            val last = merged.last()
            if (start <= last.second) {
                // Overlapping or adjacent — extend if needed
                merged[merged.lastIndex] = last.first to maxOf(last.second, end)
            } else {
                merged.add(start to end)
            }
        }
        return merged
    }

    /**
     * Builds [PenaltyLine] entries for every uncovered gap inside the billing period.
     *
     * @param periodStart    Start of the billing period (epoch ms)
     * @param periodEnd      End of the billing period (epoch ms)
     * @param totalHours     Total duration of the billing period in hours
     * @param totalEnergyKwh Total energy consumed during the billing period (kWh)
     * @param covered        Sorted, merged list of covered intervals
     * @param penaltyPrice   [TariffConfig.penaltyPricePerKwh]
     */
    private fun buildPenaltyLines(
        periodStart: Long,
        periodEnd: Long,
        totalHours: Double,
        totalEnergyKwh: Double,
        covered: List<Pair<Long, Long>>,
        penaltyPrice: Double
    ): List<PenaltyLine> {
        if (totalHours <= 0.0) return emptyList()

        val gaps = mutableListOf<PenaltyLine>()
        var cursor = periodStart

        for ((covStart, covEnd) in covered) {
            if (cursor < covStart) {
                // Gap before this covered interval
                gaps.add(
                    createPenaltyLine(cursor, covStart, totalHours, totalEnergyKwh, penaltyPrice)
                )
            }
            // Advance cursor past this covered interval
            cursor = maxOf(cursor, covEnd)
        }

        // Trailing gap after last covered interval
        if (cursor < periodEnd) {
            gaps.add(
                createPenaltyLine(cursor, periodEnd, totalHours, totalEnergyKwh, penaltyPrice)
            )
        }

        return gaps
    }

    private fun createPenaltyLine(
        gapStart: Long,
        gapEnd: Long,
        totalHours: Double,
        totalEnergyKwh: Double,
        penaltyPrice: Double
    ): PenaltyLine {
        val durationMs    = maxOf(0L, gapEnd - gapStart)
        val durationHours = durationMs / 3_600_000.0
        val energyKwh     = if (totalHours > 0.0)
            (durationHours / totalHours) * totalEnergyKwh
        else 0.0
        val cost          = energyKwh * penaltyPrice

        return PenaltyLine(
            gapStart      = gapStart,
            gapEnd        = gapEnd,
            durationHours = durationHours,
            energyKwh     = energyKwh,
            cost          = cost
        )
    }
}
