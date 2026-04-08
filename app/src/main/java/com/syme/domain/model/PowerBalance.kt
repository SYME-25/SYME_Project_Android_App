package com.syme.domain.model

data class PowerBalance(
    val activePowerW: Double,       // P  (W)
    val reactivePowerVar: Double,   // Q  (VAR)
    val apparentPowerVa: Double,    // S  (VA)
    val currentTotalA: Double,      // I  (A)
    val powerFactor: Double         // cos φ
)
