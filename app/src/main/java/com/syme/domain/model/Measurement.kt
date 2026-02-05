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
    val voltage: Double? = null,
    val current: Double? = null,

    // Puissances
    val activePowerW: Double? = null,       // Puissance active (W)
    val reactivePowerVar: Double? = null,   // Puissance réactive (var)
    val apparentPowerVA: Double? = null,    // Puissance apparente (VA)

    // Énergies
    val energyActiveWh: Double? = null,     // Énergie active (Wh)
    val energyReactiveVarh: Double? = null, // Énergie réactive (varh)
    val energyApparentVAh: Double? = null,  // Énergie apparente (VAh)

    val aiAnalysisStatus: String = "pending",  // "pending" | "processed" | "flagged"
    val rawPayload: Map<String, Any>? = null   // Données brutes optionnelles
)
