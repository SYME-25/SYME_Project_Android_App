package com.syme.ui.snapshot

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.syme.R

/**
 * Composable unique pour afficher tous les messages globaux.
 * Affiché en bas de l’écran (style Snackbar / Facebook).
 */
@Composable
fun GlobalMessageSnapshot(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val messageState by globalMessageManager.message

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        messageState?.let { message ->

            val (iconTint, containerColor) = if (message.isSystemMessage) {
                val noInternetText = context.getString(R.string.system_no_internet)
                if (message.text.contains(noInternetText, ignoreCase = true)) {
                    MaterialTheme.colorScheme.error to MessageColors.surfaceColor()
                } else {
                    MessageColors.SuccessGreen to MessageColors.surfaceColor()
                }
            } else {
                if (message.isError) MessageColors.errorIcon() to MessageColors.surfaceColor()
                else MessageColors.SuccessGreen to MessageColors.surfaceColor()
            }

            MessageSnapshotCard(
                visible = true,
                message = message.text,
                icon = message.icon,
                iconTint = iconTint,
                containerColor = containerColor,
                modifier = Modifier
                    .padding(
                        bottom = 80.dp,      // ⬆️ on le remonte
                        start = 16.dp,       // ⬅️ marge gauche
                        end = 16.dp          // ➡️ marge droite
                    )
            )

            LaunchedEffect(message) {
                val connectionRestoredText =
                    context.getString(R.string.system_connection_restored)
                if (!message.isSystemMessage || message.text.contains(connectionRestoredText)) {
                    delay(2500)
                    if (globalMessageManager.message.value == message) {
                        globalMessageManager.clearMessage()
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewGlobalMessageSnapshot() {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val roomText = context.getString(R.string.preview_item_room)
        globalMessageManager.showMessage(
            item = roomText,
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
