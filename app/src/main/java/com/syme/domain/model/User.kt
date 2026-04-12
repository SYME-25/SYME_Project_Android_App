package com.syme.domain.model

data class User(
    val userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val birthday: Long? = null,
    val gender: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val fcmTokens: List<String> = emptyList(),
    val roles: Map<String, Boolean>? = null,
    val metadata: Map<String, Any>? = null,
    val trace: Traceability = Traceability()
)