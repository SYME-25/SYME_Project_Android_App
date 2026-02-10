package com.syme.domain.mapper

import com.syme.R
import com.syme.domain.model.enumeration.CircuitType

val CircuitType.labelResId: Int
    get() = when (this) {

        // 🏠 Domestique
        CircuitType.PROTECTED -> R.string.home_circuit_protected
        CircuitType.UNPROTECTED -> R.string.home_circuit_unprotected
    }