package com.syme.ui.screen.bot.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.sin
import kotlin.math.abs

/**
 * A live waveform bar indicator driven by [amplitude] (0f–1f).
 *
 * Each bar has a fixed "base" height fraction so the component always looks
 * alive, but the bars grow proportionally to the voice amplitude so it
 * visually reacts to real sound rather than oscillating identically.
 *
 * @param amplitude  Normalised RMS level from the microphone, 0f = silence, 1f = peak.
 */
@Composable
fun VoiceWaveIndicator(
    modifier: Modifier = Modifier,
    color: Color,
    amplitude: Float = 0f,          // ← driven by onRmsChanged, NOT an infinite animation
    barCount: Int = 7
) {
    // Each bar has a different "natural" height so the shape looks like a wave.
    // Heights are defined as a fraction of the max bar height.
    val baseHeights = remember(barCount) {
        List(barCount) { i ->
            // Sine curve over the bars: centre bars taller, edge bars shorter
            val angle = Math.PI * i / (barCount - 1).coerceAtLeast(1)
            0.25f + 0.35f * sin(angle).toFloat()   // range ≈ [0.25, 0.60]
        }
    }

    Row(
        modifier              = modifier.height(32.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        baseHeights.forEachIndexed { index, base ->
            // Each bar's target height = base + (amplitude contribution).
            // A small phase offset per bar keeps them from all moving in lockstep.
            val phaseOffset = abs(sin((index + 1).toDouble() * 0.7)).toFloat() * 0.15f
            val targetFraction = (base + amplitude * (0.4f + phaseOffset)).coerceIn(0f, 1f)

            val animatedFraction by animateFloatAsState(
                targetValue   = targetFraction,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness    = Spring.StiffnessMedium
                ),
                label = "bar_$index"
            )

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(animatedFraction)
                    .background(
                        color = color.copy(alpha = 0.6f + animatedFraction * 0.4f),
                        shape = RoundedCornerShape(50)
                    )
            )
        }
    }
}

// Make kotlin.math accessible without import clash
private fun remember(barCount: Int, block: () -> List<Float>) = block()
