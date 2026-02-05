package com.syme.domain.model

data class ConsumptionBar(
    val timeLabel: String = "00",   // "01", "03", "05"…
    val subscription: Float = 0f, // partie fixe
    val consumption: Float = 0f,   // partie variable
    val injection: Float = 0f
)
