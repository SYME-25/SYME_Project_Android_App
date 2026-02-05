package com.syme.ui.snapshot

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay

/**
 * Composable unique pour afficher tous les messages globaux.
 * Style identique à ConnectivitySnapshot.
 */
@Composable
fun GlobalMessageSnapshot(modifier: Modifier = Modifier) {
    val messageState by globalMessageManager.message

    messageState?.let { message ->

        val (iconTint, containerColor) = if (message.isSystemMessage) {
            // Style identique à ConnectivitySnapshot
            if (message.text.contains("No internet", ignoreCase = true)) {
                MaterialTheme.colorScheme.error to MessageColors.surfaceColor()
            } else {
                MessageColors.SuccessGreen to MessageColors.surfaceColor()
            }
        } else {
            // Style classique pour CRUD
            if (message.isError) MessageColors.errorIcon() to MessageColors.surfaceColor()
            else MessageColors.SuccessGreen to MessageColors.surfaceColor()
        }

        MessageSnapshotCard(
            visible = true,
            message = message.text,
            icon = message.icon,
            iconTint = iconTint,
            containerColor = containerColor,
            modifier = modifier
        )

        // Disparition automatique après délai pour messages CRUD et messages système temporaires
        LaunchedEffect(message) {
            if (!message.isSystemMessage || message.text.contains("Connection restored")) {
                delay(2500)
                // On clear seulement si le message courant est toujours celui qu'on veut
                if (globalMessageManager.message.value == message) {
                    globalMessageManager.clearMessage()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewGlobalMessageSnapshot() {
    // On injecte temporairement un message pour la preview
    LaunchedEffect(Unit) {
        globalMessageManager.showMessage(
            item = "Room",
            action = MessageAction.DELETE,
            type = MessageType.SUCCESS
        )
        delay(3000)
        globalMessageManager.clearMessage()
    }

    MaterialTheme {
        GlobalMessageSnapshot()
    }
}
