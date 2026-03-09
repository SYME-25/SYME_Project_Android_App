package com.syme.domain.model

/**
 * Represents an electrical measurement captured by a meter.
 * This model is used in domain and network layers.
 * Default values allow Firebase Realtime Database to deserialize properly.
 */
data class Measurement(
    val timestamp: Long = 0L,
    val meterId: String = "",
    val installationId: String = "",
    val voltage: Double? = 0.0,
    val current: Double? = 0.0,

    // Puissances
    val activePowerW: Double? = 0.0,       // Puissance active (W)
    val reactivePowerVar: Double? = 0.0,   // Puissance réactive (var)
    val apparentPowerVA: Double? = 0.0,    // Puissance apparente (VA)

    // Énergies
    val energyActiveWh: Double? = 0.0,     // Énergie active (Wh)
    val energyReactiveVarh: Double? = 0.0, // Énergie réactive (varh)
    val energyApparentVAh: Double? = 0.0,  // Énergie apparente (VAh)

    val aiAnalysisStatus: String = "pending",  // "pending" | "processed" | "flagged"
    val rawPayload: Map<String, Any>? = null   // Données brutes optionnelles
)
