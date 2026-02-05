package com.syme.utils


object RegexUtils {
    val passwordRegex = Regex(
        "^(?=.*[0-9])(?=.*[!@#\$%^&*()_+\\-\\=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$"
    )
}