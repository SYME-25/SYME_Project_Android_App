package com.syme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syme.data.remote.user.model.UserFirebase
import com.syme.data.remote.user.repository.AuthRepository
import com.syme.domain.model.Traceability
import com.syme.utils.TimeUtils
import com.syme.utils.buildTraceability
import com.syme.utils.generateAccountId
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val repository = AuthRepository()

    fun register(
        firstName: String,
        lastName: String,
        birthday: Long?,
        gender: String,
        phone: String,
        address: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 1. Génération ID compte
                val userId = generateAccountId()

                // 2. Traceability (création)
                val trace: Traceability = buildTraceability(
                    existing = null,              // création
                    currentUserId = userId,       // l’utilisateur se crée lui-même
                    currentUserRole = "USER"
                )

                // 3. Metadata (privacy policy)
                val metadata = mapOf(
                    "privacyPolicy" to mapOf(
                        "accepted" to true,
                        "acceptedAt" to TimeUtils.currentTimestamp,
                        "version" to "1.0"
                    )
                )

                // 4. Construction UserFirebase
                val user = UserFirebase(
                    userId = userId,
                    firstName = firstName,
                    lastName = lastName,
                    birthday = birthday,
                    gender = gender,
                    phone = phone,
                    address = address,
                    email = email,
                    metadata = metadata,
                    trace = trace
                )

                // 5. Firebase Auth + Firestore
                repository.registerUser(
                    email = email,
                    password = password,
                    user = user
                )

                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Registration failed")
            }
        }
    }
}
