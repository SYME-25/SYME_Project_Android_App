package com.syme.utils

import android.os.Build
import androidx.annotation.RequiresApi
import com.syme.domain.model.Consumption
import java.time.*
import java.time.temporal.ChronoUnit

object SubscriptionUtils {

    @RequiresApi(Build.VERSION_CODES.O)
    private val zone = ZoneId.systemDefault()

    @RequiresApi(Build.VERSION_CODES.O)
    fun getActiveSubscription(
        consumptions: List<Consumption>,
        date: LocalDate
    ): Consumption? {

        val epoch = date.atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        return consumptions.firstOrNull {
            epoch in it.periodStart..it.periodEnd
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getSubscriptionDailyShare(
        subscription: Consumption
    ): Float {

        val start = Instant.ofEpochMilli(subscription.periodStart)
            .atZone(zone)
            .toLocalDate()

        val end = Instant.ofEpochMilli(subscription.periodEnd)
            .atZone(zone)
            .toLocalDate()

        val totalDays = ChronoUnit.DAYS.between(start, end) + 1

        if (totalDays <= 0) return 0f

        return subscription.totalEnergy_kWh.toFloat() / totalDays
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCoveredDaysInMonth(
        subscription: Consumption,
        year: Int,
        month: Int
    ): Long {

        val start = Instant.ofEpochMilli(subscription.periodStart)
            .atZone(zone)
            .toLocalDate()

        val end = Instant.ofEpochMilli(subscription.periodEnd)
            .atZone(zone)
            .toLocalDate()

        val monthStart = LocalDate.of(year, month, 1)
        val monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth())

        val effectiveStart = maxOf(start, monthStart)
        val effectiveEnd = minOf(end, monthEnd)

        if (effectiveStart > effectiveEnd) return 0

        return ChronoUnit.DAYS.between(effectiveStart, effectiveEnd) + 1
    }
}