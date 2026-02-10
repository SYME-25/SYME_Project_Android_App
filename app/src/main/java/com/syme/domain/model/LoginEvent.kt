package com.syme.domain.model


// Events pour le Composable
sealed class LoginEvent {
    data class Success(val messageRes: Int) : LoginEvent()
    data class Error(val messageRes: Int, val arg: String? = null) : LoginEvent()
}
