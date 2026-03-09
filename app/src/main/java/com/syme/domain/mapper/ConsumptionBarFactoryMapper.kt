package com.syme.domain.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.syme.domain.model.*
import com.syme.domain.model.enumeration.PeriodFilter
import com.syme.utils.MeasurementGrouper
import com.syme.utils.SubscriptionUtils
import java.time.*
import kotlin.math.max

object ConsumptionBarFactory {

    @RequiresApi(Build.VERSION_CODES.O)
    private val zone = ZoneId.systemDefault()

    @RequiresApi(Build.VERSION_CODES.O)
    fun build(
        measurements: List<Measurement>,
        consumptions: List<Consumption>,
        periodFilter: PeriodFilter,
        referenceDate: LocalDate
    ): List<ConsumptionBar> {

        val subscription = SubscriptionUtils
            .getActiveSubscription(consumptions, referenceDate)

        val dailyShare = subscription?.let {
            SubscriptionUtils.getSubscriptionDailyShare(it)
        } ?: 0f

        return when (periodFilter) {

            PeriodFilter.DAY -> buildDay(
                measurements,
                dailyShare
            )

            PeriodFilter.MONTH -> buildMonth(
                measurements,
                subscription,
                dailyShare,
                referenceDate
            )

            PeriodFilter.YEAR -> buildYear(
                measurements,
                subscription,
                referenceDate
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildDay(
        measurements: List<Measurement>,
        dailyShare: Float
    ): List<ConsumptionBar> {

        val grouped = MeasurementGrouper.groupByHour(measurements)
        val hourlySubscription = dailyShare / 24f

        return (0..23).map { hour ->

            val group = grouped[hour] ?: emptyList()
            val hasData = group.isNotEmpty()

            val consumption = group
                .mapNotNull { it.energyActiveWh }
                .sum()
                .toFloat()
                .coerceAtLeast(0f)

            val injection = group
                .mapNotNull { it.energyApparentVAh }
                .sum()
                .toFloat()
                .coerceAtLeast(0f)

            val subscriptionValue =
                if (hasData) hourlySubscription else 0f

            ConsumptionBar(
                timeLabel = "${hour}h",
                subscription = subscriptionValue,
                consumption = consumption,
                injection = injection
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildMonth(
        measurements: List<Measurement>,
        subscription: Consumption?,
        dailyShare: Float,
        referenceDate: LocalDate
    ): List<ConsumptionBar> {

        val grouped = MeasurementGrouper.groupByDay(measurements)
        val daysInMonth = referenceDate.lengthOfMonth()

        return (1..daysInMonth).map { day ->

            val group = grouped[day] ?: emptyList()
            val hasData = group.isNotEmpty()

            val consumption = group
                .mapNotNull { it.energyActiveWh }
                .sum()
                .toFloat()
                .coerceAtLeast(0f)

            val injection = group
                .mapNotNull { it.energyApparentVAh }
                .sum()
                .toFloat()
                .coerceAtLeast(0f)

            val subscriptionValue =
                if (
                    hasData &&
                    subscription != null &&
                    isDayCovered(subscription, referenceDate.year, referenceDate.monthValue, day)
                ) dailyShare
                else 0f

            ConsumptionBar(
                timeLabel = day.toString(),
                subscription = subscriptionValue,
                consumption = consumption,
                injection = injection
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildYear(
        measurements: List<Measurement>,
        subscription: Consumption?,
        referenceDate: LocalDate
    ): List<ConsumptionBar> {

        val grouped = MeasurementGrouper.groupByMonth(measurements)

        return (1..12).map { month ->

            val group = grouped[month] ?: emptyList()
            val hasData = group.isNotEmpty()

            val consumption = group
                .mapNotNull { it.energyActiveWh }
                .sum()
                .toFloat()
                .coerceAtLeast(0f)

            val injection = group
                .mapNotNull { it.energyApparentVAh }
                .sum()
                .toFloat()
                .coerceAtLeast(0f)

            val subscriptionValue = if (hasData && subscription != null) {
                val coveredDays = SubscriptionUtils
                    .getCoveredDaysInMonth(subscription, referenceDate.year, month)

                val daily = SubscriptionUtils
                    .getSubscriptionDailyShare(subscription)

                coveredDays * daily
            } else 0f

            ConsumptionBar(
                timeLabel = month.toString(),
                subscription = subscriptionValue,
                consumption = consumption,
                injection = injection
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isDayCovered(
        subscription: Consumption,
        year: Int,
        month: Int,
        day: Int
    ): Boolean {

        val date = LocalDate.of(year, month, day)
        val epoch = date.atStartOfDay(zone).toInstant().toEpochMilli()

        return epoch in subscription.periodStart..subscription.periodEnd
    }
}