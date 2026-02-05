package com.syme.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syme.data.remote.model.UserFirebase
import com.syme.data.remote.repository.AuthRepository
import com.syme.domain.model.Traceability
import com.syme.ui.state.UiState
import com.syme.utils.TimeUtils
import com.syme.utils.buildTraceability
import com.syme.utils.generateId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState

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
            _uiState.value = UiState.Loading
            try {
                val userId = generateId()

                val trace: Traceability = buildTraceability(
                    existing = null,
                    currentUserId = userId,
                    currentUserRole = "USER"
                )

                val metadata = mapOf(
                    "privacyPolicy" to mapOf(
                        "accepted" to true,
                        "acceptedAt" to TimeUtils.currentTimestamp,
                        "version" to "1.0"
                    )
                )

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

                repository.registerUser(
                    email = email,
                    password = password,
                    user = user
                )

                _uiState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _uiState.value =
                    UiState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
}
