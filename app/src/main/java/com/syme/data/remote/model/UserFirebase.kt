package com.syme.data.remote.model

import com.syme.domain.model.Traceability

data class UserFirebase(
    val userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val birthday: Long? = null,
    val gender: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val roles: Map<String, Boolean> = mapOf("USER" to true),
    val metadata: Map<String, Any> = emptyMap(),
    val trace: Traceability = Traceability()
)