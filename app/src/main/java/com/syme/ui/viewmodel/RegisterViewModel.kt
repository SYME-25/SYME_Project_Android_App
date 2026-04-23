package com.syme.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syme.R
import com.syme.data.repository.AuthRepository
import com.syme.domain.model.RegisterEvent
import com.syme.domain.model.Traceability
import com.syme.domain.model.User
import com.syme.utils.TimeUtils
import com.syme.utils.buildTraceability
import com.syme.utils.generateId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _registerEvent = MutableSharedFlow<RegisterEvent>()
    val registerEvent: SharedFlow<RegisterEvent> = _registerEvent

    fun register(
        firstName: String,
        lastName: String,
        birthday: Long?,
        gender: String,
        phone: String,
        address: String,
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val userId = generateId()

                val trace: Traceability = buildTraceability(
                    existing = null,
                    currentUserId = userId,
                    currentUserRole = "USER",
                    isActive = true
                )

                val metadata = mapOf(
                    "lang" to "en",
                    "privacyPolicy" to mapOf(
                        "accepted" to true,
                        "acceptedAt" to TimeUtils.currentTimestamp,
                        "version" to "1.0"
                    )
                )

                val user = User(
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

                repository.registerUser(
                    email = email,
                    password = password,
                    user = user
                )

                _registerEvent.emit(
                    RegisterEvent.Success(R.string.register_success)
                )

            } catch (e: Exception) {
                _registerEvent.emit(
                    RegisterEvent.Error(
                        R.string.register_error_with_message,
                        e.message ?: "Unknown error"
                    )
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
}
