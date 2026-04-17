package com.syme.domain.mapper

import androidx.annotation.StringRes
import com.syme.R
import com.syme.domain.model.enumeration.AppLanguage
import com.syme.domain.model.enumeration.AppTheme

/**
 * Maps each AppTheme to its display string resource.
 */
val AppTheme.labelRes: Int
    @StringRes get() = when (this) {
        AppTheme.SYSTEM -> R.string.settings_theme_system
        AppTheme.LIGHT  -> R.string.settings_theme_light
        AppTheme.DARK   -> R.string.settings_theme_dark
    }

/**
 * Maps each AppLanguage to its display string resource.
 */
val AppLanguage.labelRes: Int
    @StringRes get() = when (this) {
        AppLanguage.ENGLISH -> R.string.settings_language_english
        AppLanguage.FRENCH  -> R.string.settings_language_french
    }
