package com.syme.ui.alerts

import java.util.*

/**
 * Représente une alerte/dialogue dans l'application.
 */
data class Alert(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Alert",
    val message: String,
    val confirmText: String = "OK",
    val cancelText: String? = null,
    val onConfirm: (() -> Unit)? = null,
    val onCancel: (() -> Unit)? = null
)
