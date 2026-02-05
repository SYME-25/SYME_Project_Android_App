package com.syme.domain.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.syme.domain.model.ConsumptionBar
import com.syme.domain.model.Measurement
import com.syme.domain.model.enumeration.PeriodFilter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object MeasurementConverter {

    /**
     * Convertit une liste de Measurement en liste de ConsumptionBar pour le graphique,
     * en fonction de la période sélectionnée : JOUR, MOIS ou ANNÉE
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun measurementsToConsumptionBars(
        measurements: List<Measurement>,
        periodFilter: PeriodFilter = PeriodFilter.DAY,
        referenceDate: LocalDate = LocalDate.now()
    ): List<ConsumptionBar> {

        if (measurements.isEmpty()) {
            // Générer 4 barres vides par défaut
            return List(4) { index ->
                ConsumptionBar(
                    timeLabel = "J${index + 1}",
                    subscription = 0f,
                    consumption = 0f
                )
            }
        }

        // Grouper les mesures selon le filtre
        val grouped: Map<String, List<Measurement>> = when (periodFilter) {
            PeriodFilter.DAY -> {
                // Par heure de la journée
                measurements.groupBy { measurement ->
                    Instant.ofEpochMilli(measurement.timestamp)
                        .atZone(ZoneId.systemDefault())
                        .hour.toString()
                }
            }
            PeriodFilter.MONTH -> {
                // Par jour du mois
                measurements.groupBy { measurement ->
                    val localDate = Instant.ofEpochMilli(measurement.timestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    localDate.dayOfMonth.toString()
                }
            }
            PeriodFilter.YEAR -> {
                // Par mois de l'année
                measurements.groupBy { measurement ->
                    val localDate = Instant.ofEpochMilli(measurement.timestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    localDate.monthValue.toString()
                }
            }
        }

        // Transformer en ConsumptionBar
        return grouped.map { (label, group) ->
            val consumption = group.sumOf { it.energyActiveWh ?: 0.0 }.toFloat() / 1000f
            val injection = group.sumOf { it.energyApparentVAh ?: 0.0 }.toFloat() / 1000f

            ConsumptionBar(
                timeLabel = when (periodFilter) {
                    PeriodFilter.DAY -> "${label}h"
                    PeriodFilter.MONTH -> "J$label"
                    PeriodFilter.YEAR -> "M$label"
                },
                subscription = 0f, // Aucun abonnement pour l'instant
                consumption = consumption,
            )
        }.sortedBy { it.timeLabel }
    }

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
