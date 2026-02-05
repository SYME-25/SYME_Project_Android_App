package com.syme.domain.model

data class Location(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val accuracy_m: Float = 0f,
    val lastUpdatedEpochMs: Long = System.currentTimeMillis()
)
