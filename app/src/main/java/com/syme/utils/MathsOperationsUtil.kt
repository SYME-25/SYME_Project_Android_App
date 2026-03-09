package com.syme.utils

import kotlin.math.roundToInt

fun round2(value: Double?): Double? =
    value?.let { (it * 100).roundToInt() / 100.0 }
