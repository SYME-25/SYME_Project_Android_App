package com.syme.domain.model

import com.syme.domain.model.enumeration.CircuitState

data class Circuit(
    val circuitId: String = "",
    val installationId: String = "",
    val meterId: String = "",
    val relayChannel: Int? = null,          // ex: 1, 2, 3, 4...

    val name: String = "",                  // "Kitchen Lighting", "AC Main"

    val priority: Int = 0,                  // Déclenchement en cas de délestage

    val isProtected: Boolean = false,       // Circuit critique (ex: réfrigérateur)
    val currentState: CircuitState = CircuitState.OFF,       // ON / OFF / TRIPPED

    val metadata: Map<String, Any>? = null,
    val trace: Traceability = Traceability()
)
