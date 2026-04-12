package com.syme.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.syme.R
import com.syme.data.repository.AuthRepository
import com.syme.domain.model.ResetPasswordEvent
import com.syme.domain.state.UiState

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<ResetPasswordEvent>()
    val event: SharedFlow<ResetPasswordEvent> = _event.asSharedFlow()

    fun sendResetEmail(email: String) {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                repository.sendPasswordResetEmail(email)
                _uiState.value = UiState.Success(Unit)
                _event.emit(ResetPasswordEvent.Success(R.string.password_reset_email_sent))
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
                _event.emit(
                    ResetPasswordEvent.Error(
                        messageRes = R.string.password_reset_email_error,
                        arg        = e.message ?: ""
                    )
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
}