package com.syme.ui.snapshot

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.vector.ImageVector
import com.syme.R

/**
 * Gestion centralisée des messages.
 */
class GlobalMessageManager(private val context: Context) {

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
            isSystemMessage -> item ?: context.getString(R.string.system_message)

            else -> when (type) {
                MessageType.SUCCESS -> when (action) {
                    MessageAction.CREATE -> context.getString(R.string.created_success, item)
                    MessageAction.UPDATE -> context.getString(R.string.updated_success, item)
                    MessageAction.DELETE -> context.getString(R.string.deleted_success, item)
                    null -> item ?: context.getString(R.string.success)
                }

                MessageType.ERROR -> when (action) {
                    MessageAction.CREATE -> context.getString(R.string.create_failed, item)
                    MessageAction.UPDATE -> context.getString(R.string.update_failed, item)
                    MessageAction.DELETE -> context.getString(R.string.delete_failed, item)
                    null -> item ?: context.getString(R.string.error)
                }

                MessageType.INFO -> item ?: context.getString(R.string.information)
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

/* ✅ CONTEXTE GLOBAL */
lateinit var appContext: Context

/* ✅ SINGLETON */
val globalMessageManager: GlobalMessageManager by lazy {
    GlobalMessageManager(appContext)
}
