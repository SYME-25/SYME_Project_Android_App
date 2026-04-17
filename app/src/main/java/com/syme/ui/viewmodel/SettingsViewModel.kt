package com.syme.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syme.data.preferences.UserPreferencesRepository
import com.syme.domain.model.enumeration.AppLanguage
import com.syme.domain.model.enumeration.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _theme = MutableStateFlow(AppTheme.SYSTEM)
    val theme: StateFlow<AppTheme> = _theme.asStateFlow()

    private val _language = MutableStateFlow(AppLanguage.ENGLISH)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.getTheme().collect { _theme.value = it }
        }
        viewModelScope.launch {
            preferencesRepository.getLanguage().collect { _language.value = it }
        }
        viewModelScope.launch {
            preferencesRepository.getNotificationsEnabled().collect { _notificationsEnabled.value = it }
        }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            preferencesRepository.setTheme(theme)
            _theme.value = theme
        }
    }

    fun setLanguage(language: AppLanguage) {
        _language.value = language  // ✅ Mise à jour immédiate du StateFlow
        viewModelScope.launch {
            preferencesRepository.setLanguage(language)  // Persistance en arrière-plan
        }

        Log.d("LANG_DEBUG", "Setting: ${language.tag}")
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setNotificationsEnabled(enabled)
            _notificationsEnabled.value = enabled
        }
    }
}
