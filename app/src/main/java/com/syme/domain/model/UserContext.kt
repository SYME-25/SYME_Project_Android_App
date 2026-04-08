package com.syme.domain.model


/**
 * Contexte utilisateur passé depuis le ViewModel.
 * Tous les champs sont optionnels — les nulls sont simplement omis du prompt.
 */
data class UserContext(
    val userName: String? = null,
    val installationName: String? = null,
    val installationType: String? = null,        // ex: "résidentielle", "industrielle"
    val totalEnergyWh: Double? = null,
    val applianceCount: Int? = null,
    val applianceSummary: String? = null,        // ex: "Frigo 150W, Climatiseur 1200W"
    val lastBillAmountXaf: Double? = null,
    val lastConsumptionKwh: Double? = null,
    val tariffXafPerKwh: Double? = null,
    val circuitCount: Int? = null,
    val meterCount: Int? = null
)
