package com.syme.domain.model

sealed class MeterEvent {
    data class Success(val messageRes: Int) : MeterEvent()
    data class Error(val messageRes: Int, val arg: String? = null) : MeterEvent()
}
