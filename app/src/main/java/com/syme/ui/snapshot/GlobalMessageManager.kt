package com.syme.ui.snapshot

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Gestion centralisée des messages.
 */
class GlobalMessageManager {

    private val _message: MutableState<Message?> = mutableStateOf(null)
    val message: State<Message?> = _message

    fun showMessage(
        item: String? = null,
        action: MessageAction? = null,
        type: MessageType,
        customText: String? = null,
        customIcon: ImageVector? = null,
        isSystemMessage: Boolean = false
    ) {
        val text = customText ?: when {
            isSystemMessage -> item ?: "System Message"

            else -> when (type) {
                MessageType.SUCCESS -> when (action) {
                    MessageAction.CREATE -> "$item created successfully"
                    MessageAction.UPDATE -> "$item updated successfully"
                    MessageAction.DELETE -> "$item deleted successfully"
                    null -> item ?: "Success"
                }

                MessageType.ERROR -> when (action) {
                    MessageAction.CREATE -> "$item failed to create"
                    MessageAction.UPDATE -> "$item failed to update"
                    MessageAction.DELETE -> "$item failed to delete"
                    null -> item ?: "Error"
                }

                MessageType.INFO -> item ?: "Information"
            }
        }

        val icon = customIcon ?: when {
            isSystemMessage -> Icons.Default.Wifi
            type == MessageType.SUCCESS -> Icons.Default.CheckCircle
            type == MessageType.ERROR -> Icons.Default.Close
            type == MessageType.INFO -> Icons.Default.Info
            else -> Icons.Default.Info
        }

        _message.value = Message(
            text = text,
            icon = icon,
            isError = type == MessageType.ERROR && !isSystemMessage,
            isSystemMessage = isSystemMessage
        )
    }

    fun clearMessage() {
        _message.value = null
    }
}

// Singleton accessible globalement
val globalMessageManager = GlobalMessageManager()
