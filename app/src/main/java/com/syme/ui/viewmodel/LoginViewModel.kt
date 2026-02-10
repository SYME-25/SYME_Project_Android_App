package com.syme.ui.viewmodel

import android.util.Log
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syme.R
import com.syme.data.remote.repository.AuthRepository
import com.syme.domain.model.LoginEvent
import com.syme.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    private val _loginEvent = MutableSharedFlow<LoginEvent>()
    val loginEvent: SharedFlow<LoginEvent> = _loginEvent.asSharedFlow()

    fun login(email: String, password: String) {
        _uiState.value = UiState.Loading

        viewModelScope.launch {
            try {
                repository.login(email, password)
                _uiState.value = UiState.Success(Unit)

                _loginEvent.emit(
                    LoginEvent.Success(R.string.login_success)
                )

            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    e.message ?: "Login failed"
                )

                _loginEvent.emit(
                    LoginEvent.Error(
                        messageRes = R.string.login_error_with_message,
                        arg = e.message ?: "Login failed"
                    )
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
}
