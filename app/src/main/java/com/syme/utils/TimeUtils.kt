package com.syme.utils

import java.text.SimpleDateFormat
import java.util.Locale

object TimeUtils {

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val currentTimestamp = System.currentTimeMillis()

}