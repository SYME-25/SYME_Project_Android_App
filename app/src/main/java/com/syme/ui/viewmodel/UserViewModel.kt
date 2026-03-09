package com.syme.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syme.R
import com.syme.data.remote.repository.UserRepository
import com.syme.domain.model.User
import com.syme.domain.model.UserEvent
import com.syme.ui.state.UiState
import com.syme.utils.buildTraceability
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val userState: StateFlow<UiState<User>> = _userState

    private val _userEvent = MutableSharedFlow<UserEvent>()
    val userEvent: SharedFlow<UserEvent> = _userEvent

    // ─── Load current user ─────────────────────────────────

    fun loadUser(userId: String) {
        viewModelScope.launch {
            _userState.value = UiState.Loading
            try {
                val user = repository.getOnce(userId)

                if (user != null) {
                    _userState.value = UiState.Success(user)
                } else {
                    _userState.value = UiState.Error("User not found")
                }

            } catch (e: Exception) {
                _userState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // ─── Update user ───────────────────────────────────────

    fun updateUser(
        currentUserId: String,
        currentUserRole: String = "USER",
        firstName: String? = null,
        lastName: String? = null,
        birthday: Long? = null,
        gender: String? = null,
        phone: String? = null,
        address: String? = null
    ) {

        val current = (_userState.value as? UiState.Success<User>)?.data ?: return

        viewModelScope.launch {

            try {

                val updatedUser = current.copy(
                    firstName = firstName ?: current.firstName,
                    lastName = lastName ?: current.lastName,
                    birthday = birthday ?: current.birthday,
                    gender = gender ?: current.gender,
                    phone = phone ?: current.phone,
                    address = address ?: current.address,
                    trace = buildTraceability(
                        existing = current.trace,
                        currentUserId = currentUserId,
                        currentUserRole = currentUserRole,
                        isActive = current.trace.active
                    )
                )

                repository.update(updatedUser)

                _userState.value = UiState.Success(updatedUser)

                _userEvent.emit(
                    UserEvent.Success(R.string.profile_update_success)
                )

            } catch (e: Exception) {

                _userEvent.emit(
                    UserEvent.Error(
                        R.string.profile_update_error,
                        e.message ?: "Unknown error"
                    )
                )
            }
        }
    }
}