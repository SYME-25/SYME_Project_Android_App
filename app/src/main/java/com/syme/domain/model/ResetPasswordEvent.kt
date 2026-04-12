package com.syme.domain.model

import androidx.annotation.StringRes

sealed class ResetPasswordEvent {
    data class Success(@StringRes val messageRes: Int) : ResetPasswordEvent()
    data class Error(
        @StringRes val messageRes: Int,
        val arg: String = ""
    ) : ResetPasswordEvent()
}