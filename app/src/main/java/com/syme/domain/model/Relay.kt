package com.syme.domain.model

data class Relay(
    val relayId: String = "",
    val meterId: String = "",
    val channel: Int = 0,               // ex: 1, 2, 3, 4...
    val currentState: String = "OFF" ,  // ON / OFF
    val maxCurrent: Double? = null,     // Sécurité électrique
)
