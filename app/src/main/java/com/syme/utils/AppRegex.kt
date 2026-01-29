package com.syme.utils

val passwordRegex = Regex(
    "^(?=.*[0-9])(?=.*[!@#\$%^&*()_+\\-\\=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$"
)
