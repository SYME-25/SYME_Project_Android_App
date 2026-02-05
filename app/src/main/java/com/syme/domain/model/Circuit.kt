package com.syme.domain.model

data class Circuit(
    val circuitId: String = "",
    val householdId: String = "",
    val meterId: String = "",
    val relayChannel: Int? = null,          // ex: 1, 2, 3, 4...

    val name: String = "",                  // "Kitchen Lighting", "AC Main"

    val maxAllowedPower: Double? = null,    // Limite de sécurité (W)
    val priority: Int = 0,                  // Déclenchement en cas de délestage

    val isProtected: Boolean = false,       // Circuit critique (ex: réfrigérateur)
    val currentState: String = "OFF",       // ON / OFF / TRIPPED

    val orderIndex: Int = 0,                // pour affichage stable

    val metadata: Map<String, Any>? = null,

    val trace: Traceability = Traceability()
)
