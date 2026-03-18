package com.syme.domain.model

data class BillingMetadata(

    val trace: List<String> = emptyList(),

    val tariffSnapshot: Map<String, Double> = emptyMap(),

    val computedAt: Long = 0L
)