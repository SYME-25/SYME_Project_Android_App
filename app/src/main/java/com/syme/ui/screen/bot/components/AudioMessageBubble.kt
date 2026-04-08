package com.syme.ui.screen.bot.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * A message bubble that displays a voice recording, with:
 * - Play/Pause button
 * - Animated waveform bar visualisation (static bars, highlight sweeps during playback)
 * - Elapsed / total duration counter
 *
 * Pass [durationSeconds] to show the correct total.
 * The component simulates playback progress internally; wire [onPlayPause] to your
 * actual MediaPlayer for real audio.
 *
 * @param isUserMessage   True → bubble on the right (user). False → left (bot).
 * @param durationSeconds Length of the audio clip in seconds.
 * @param onPlayPause     Called when the user taps play/pause (connect to MediaPlayer).
 */
@Composable
fun AudioMessageBubble(
    modifier: Modifier = Modifier,
    isUserMessage: Boolean = true,
    durationSeconds: Int = 0,
    onPlayPause: (isPlaying: Boolean) -> Unit = {}
) {
    var isPlaying   by remember { mutableStateOf(false) }
    var progressSec by remember { mutableFloatStateOf(0f) }

    // Simulate progress tick while playing (replace with real MediaPlayer progress)
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying && progressSec < durationSeconds) {
                delay(100L)
                progressSec = (progressSec + 0.1f).coerceAtMost(durationSeconds.toFloat())
            }
            if (progressSec >= durationSeconds) {
                isPlaying   = false
                progressSec = 0f
            }
        }
    }

    val containerColor = if (isUserMessage)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.surfaceVariant

    val contentColor = if (isUserMessage)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    val playedColor = if (isUserMessage)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.primary

    val unplayedColor = if (isUserMessage)
        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
    else
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)

    Surface(
        modifier      = modifier.widthIn(min = 180.dp, max = 280.dp),
        shape         = RoundedCornerShape(
            topStart     = if (isUserMessage) 18.dp else 4.dp,
            topEnd       = if (isUserMessage) 4.dp else 18.dp,
            bottomStart  = 18.dp,
            bottomEnd    = 18.dp
        ),
        color         = containerColor,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Play / Pause button ───────────────────────────────────────────
            Box(
                modifier        = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        isPlaying = !isPlaying
                        onPlayPause(isPlaying)
                    },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector        = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Lecture",
                        tint               = contentColor,
                        modifier           = Modifier.size(22.dp)
                    )
                }
            }

            // ── Waveform bars ─────────────────────────────────────────────────
            val barCount    = 28
            val barHeights  = remember { List(barCount) { Random.nextFloat() * 0.65f + 0.2f } }
            val progress    = if (durationSeconds > 0) progressSec / durationSeconds else 0f

            Row(
                modifier              = Modifier
                    .weight(1f)
                    .height(28.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                barHeights.forEachIndexed { index, barFraction ->
                    val barProgress = index.toFloat() / barCount
                    val isPlayed    = barProgress <= progress
                    Box(
                        modifier = Modifier
                            .width(2.5.dp)
                            .fillMaxHeight(barFraction)
                            .clip(RoundedCornerShape(50))
                            .background(if (isPlayed) playedColor else unplayedColor)
                    )
                }
            }

            // ── Duration counter ──────────────────────────────────────────────
            val displaySec = if (isPlaying || progressSec > 0f)
                progressSec.roundToInt()
            else
                durationSeconds

            Text(
                text  = formatDuration(displaySec),
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}
