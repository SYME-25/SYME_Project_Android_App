package com.syme.utils

import android.content.Context
import com.syme.domain.model.enumeration.AppLanguage
import java.util.Locale

// com/syme/util/LocaleHelper.kt
object LocaleHelper {
    fun applyLocale(context: Context, language: AppLanguage) {
        val locale = Locale(language.tag)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}