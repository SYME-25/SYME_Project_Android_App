package com.syme.utils

import kotlin.random.Random

/**
 * Generates a standardized SYME Tracker ID for a given entity type.
 * Example: U-R-5F3A1C9B
 */
fun generateId(entityType: String = "U", code: String = "C"): String {
    val randomPart = List(8) {
        val chars = "0123456789ABCDEF"
        chars[Random.nextInt(chars.length)]
    }.joinToString("")
    return "$entityType-$code-$randomPart"
}
