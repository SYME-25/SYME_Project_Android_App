package com.syme.ui.alerts

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Singleton pour gérer les alertes/dialogues.
 * Utilise StateFlow pour Compose-friendly et réactif.
 */
object AlertManager {

    private val _alerts = MutableStateFlow<List<Alert>>(emptyList())
    val alerts: StateFlow<List<Alert>> get() = _alerts

    /**
     * Ajoute une alerte/dialogue.
     */
    fun showAlert(
        title: String = "Alert",
        message: String,
        confirmText: String = "OK",
        cancelText: String? = null,
        onConfirm: (() -> Unit)? = null,
        onCancel: (() -> Unit)? = null
    ) {
        val alert = Alert(
            title = title,
            message = message,
            confirmText = confirmText,
            cancelText = cancelText,
            onConfirm = onConfirm,
            onCancel = onCancel
        )
        _alerts.update { current -> current + alert }
    }

    /**
     * Supprime une alerte spécifique.
     */
    fun dismissAlert(alert: Alert) {
        _alerts.update { current -> current - alert }
    }

    /**
     * Supprime toutes les alertes.
     */
    fun clearAll() {
        _alerts.value = emptyList()
    }
}