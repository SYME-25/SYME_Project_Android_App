package com.syme.domain.model

data class Traceability(
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdById: String = "",
    val updatedById: String = "",
    val createdByRole: String = "",
    val updatedByRole: String = "",
    val version: Int = 1,
    val active: Boolean = true
)
