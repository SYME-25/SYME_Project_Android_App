package com.syme.domain.model

sealed class RegisterEvent {
    data class Success(val messageRes: Int) : RegisterEvent()
    data class Error(val messageRes: Int, val arg: String? = null) : RegisterEvent()
}
