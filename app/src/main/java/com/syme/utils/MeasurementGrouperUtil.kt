package com.syme.utils

import android.os.Build
import androidx.annotation.RequiresApi
import com.syme.domain.model.Measurement
import java.time.*
import java.time.temporal.ChronoField

object MeasurementGrouper {

    @RequiresApi(Build.VERSION_CODES.O)
    private val zone = ZoneId.systemDefault()

    @RequiresApi(Build.VERSION_CODES.O)
    fun groupByHour(measurements: List<Measurement>): Map<Int, List<Measurement>> {
        return measurements.groupBy {
            Instant.ofEpochMilli(it.timestamp)
                .atZone(zone)
                .hour
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun groupByDay(measurements: List<Measurement>): Map<Int, List<Measurement>> {
        return measurements.groupBy {
            Instant.ofEpochMilli(it.timestamp)
                .atZone(zone)
                .dayOfMonth
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun groupByMonth(measurements: List<Measurement>): Map<Int, List<Measurement>> {
        return measurements.groupBy {
            Instant.ofEpochMilli(it.timestamp)
                .atZone(zone)
                .monthValue
        }
    }
}