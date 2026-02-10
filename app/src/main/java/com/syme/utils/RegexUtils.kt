package com.syme.utils


object RegexUtils {
    val passwordRegex = Regex(
        "^(?=.*[0-9])(?=.*[!@#\$%^&*()_+\\-\\=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$"
    )

    val congoPhoneRegex = Regex("^(?:\\+242|242)?[ ]?(0?[4567])[0-9]{7}$")

}