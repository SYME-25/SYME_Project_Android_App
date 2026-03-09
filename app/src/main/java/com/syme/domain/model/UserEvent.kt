package com.syme.domain.model

import androidx.annotation.StringRes

sealed class UserEvent {
    data class Success(@StringRes val messageRes: Int) : UserEvent()
    data class Error(@StringRes val messageRes: Int, val arg: String? = null) : UserEvent()
}
