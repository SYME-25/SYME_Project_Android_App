package com.syme.utils

import android.os.Build
import androidx.annotation.RequiresApi
import com.syme.domain.model.Measurement
import com.syme.domain.model.enumeration.PeriodFilter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object MeasurementUtil {

    /**
     * Filtrer les measurements selon la période sélectionnée
     * pour n’inclure que celles du jour/mois/année choisis
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun filterMeasurementsByPeriod(
        measurements: List<Measurement>,
        periodFilter: PeriodFilter,
        referenceDate: LocalDate = LocalDate.now()
    ): List<Measurement> {

        return measurements.filter { measurement ->
            val localDate = Instant.ofEpochMilli(measurement.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            when (periodFilter) {
                PeriodFilter.DAY -> localDate.isEqual(referenceDate)
                PeriodFilter.MONTH -> localDate.year == referenceDate.year &&
                        localDate.monthValue == referenceDate.monthValue
                PeriodFilter.YEAR -> localDate.year == referenceDate.year
            }
        }
    }
}