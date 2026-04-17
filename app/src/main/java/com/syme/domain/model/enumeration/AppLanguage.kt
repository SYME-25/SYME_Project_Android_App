package com.syme.domain.model.enumeration

/**
 * Represents the supported app languages.
 * Add new entries here to support additional languages.
 * Each entry maps to a BCP-47 language tag used by LocaleListCompat.
 */
enum class AppLanguage(val tag: String) {
    ENGLISH("en"),
    FRENCH("fr"),
    // SPANISH("es"),   // Uncomment to add Spanish
    // ARABIC("ar"),    // Uncomment to add Arabic
}