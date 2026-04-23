package com.syme.ui.snapshot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

@Composable
fun MessageSnapshotCard(
    visible: Boolean,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = MessageColors.successIcon(),
    containerColor: Color = MessageColors.surfaceColor(),
    hazeState: HazeState? = null           // ← optionnel
) {
    val surfaceColor = MaterialTheme.colorScheme.surface

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit  = fadeOut() + slideOutVertically { it / 2 }
    ) {
        Box(
            modifier = modifier
                .clip(MaterialTheme.shapes.large)
                // Haze si disponible, sinon fallback couleur unie
                .then(
                    if (hazeState != null)
                        Modifier.hazeEffect(
                            state = hazeState,
                            style = HazeStyle(
                                backgroundColor = surfaceColor,
                                tint = HazeTint(containerColor.copy(alpha = 0.55f)),
                                blurRadius = 20.dp
                            )
                        )
                    else
                        Modifier.background(containerColor)
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.large
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (icon != null) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                    )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMessageSnapshotCard() {
    MaterialTheme {
        MessageSnapshotCard(
            visible = true,
            message = "This is a success message!",
            icon = Icons.Default.Info,
            iconTint = MessageColors.successIcon(),
            containerColor = MessageColors.surfaceColor(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMessageSnapshotCardWithoutIcon() {
    MaterialTheme {
        MessageSnapshotCard(
            visible = true,
            message = "This message has no icon",
            modifier = Modifier.fillMaxWidth()
        )
    }
}