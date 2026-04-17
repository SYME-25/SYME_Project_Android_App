package com.syme.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.syme.domain.model.enumeration.AppLanguage
import com.syme.domain.model.enumeration.AppTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val LANGUAGE = stringPreferencesKey("language")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    // ── Theme ──────────────────────────────────────────────
    fun getTheme(): Flow<AppTheme> = context.dataStore.data.map { prefs ->
        val raw = prefs[Keys.THEME] ?: AppTheme.SYSTEM.name
        runCatching { AppTheme.valueOf(raw) }.getOrDefault(AppTheme.SYSTEM)
    }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { it[Keys.THEME] = theme.name }
    }

    // ── Language ───────────────────────────────────────────
    fun getLanguage(): Flow<AppLanguage> = context.dataStore.data.map { prefs ->
        val raw = prefs[Keys.LANGUAGE] ?: AppLanguage.ENGLISH.name
        runCatching { AppLanguage.valueOf(raw) }.getOrDefault(AppLanguage.ENGLISH)
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.name }
    }

    // ── Notifications ──────────────────────────────────────
    fun getNotificationsEnabled(): Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.NOTIFICATIONS_ENABLED] ?: true
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }
}
