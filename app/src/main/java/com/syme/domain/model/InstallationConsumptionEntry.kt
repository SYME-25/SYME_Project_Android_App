package com.syme.domain.model

/**
 * Data class représentant la consommation totale d'une installation
 * sur la période sélectionnée.
 */
data class InstallationConsumptionEntry(
    val installationName: String,
    val totalEnergyWh: Double,
    val isSelected: Boolean = false
)
