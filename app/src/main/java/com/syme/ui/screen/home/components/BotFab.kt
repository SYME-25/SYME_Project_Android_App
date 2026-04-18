package com.syme.ui.screen.home.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.R
import com.syme.ui.component.animation.Animation
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.delay

private val BOT_MESSAGES = listOf(
    R.string.bot_message_1,
    R.string.bot_message_2,
    R.string.bot_message_3,
    R.string.bot_message_4,
    R.string.bot_message_6,
    R.string.bot_message_5,
    R.string.bot_message_8,
    R.string.bot_message_9,
    R.string.bot_message_7,
    R.string.bot_message_10
)

@Composable
fun BotFab(
    onClick: () -> Unit,
    hazeState: HazeState
) {
    val colorPrimary    = MaterialTheme.colorScheme.primary
    val colorAccent     = MaterialTheme.colorScheme.secondary
    val colorBubbleBg   = MaterialTheme.colorScheme.surfaceVariant
    val colorBubbleText = MaterialTheme.colorScheme.onSurfaceVariant
    val colorOnPrimary  = MaterialTheme.colorScheme.onPrimary
    val surfaceColor    = MaterialTheme.colorScheme.surface

    var initialExpanded by remember { mutableStateOf(true) }
    var bubbleVisible   by remember { mutableStateOf(false) }
    var messageIndex    by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        delay(2500)
        initialExpanded = false
        while (true) {
            delay(3000)
            bubbleVisible = true
            delay(2500)
            bubbleVisible = false
            delay(500)
            messageIndex = (messageIndex + 1) % BOT_MESSAGES.size
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ── Chat bubble ───────────────────────────────────────────────────
        AnimatedVisibility(
            visible = !initialExpanded && bubbleVisible,
            enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400)) +
                    fadeIn(tween(400)),
            exit  = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) +
                    fadeOut(tween(300))
        ) {
            ChatBubble(
                text = stringResource(id = BOT_MESSAGES[messageIndex]),
                colorBubbleBg = colorBubbleBg,
                colorBubbleText = colorBubbleText,
                colorPrimary = colorPrimary,
                colorAccent = colorAccent,
                hazeState = hazeState,
                surfaceColor = surfaceColor
            )
        }

        // ── FAB ───────────────────────────────────────────────────────────
        AnimatedContent(
            targetState = initialExpanded,
            transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(300)) },
            label = "BotFabTransition"
        ) { isExpanded ->
            if (isExpanded) {
                Box(
                    modifier = Modifier
                        .shadow(8.dp, RoundedCornerShape(32.dp))
                        .clip(RoundedCornerShape(32.dp))
                        .hazeEffect(
                            state = hazeState,
                            style = HazeStyle(
                                backgroundColor = surfaceColor,
                                tint = HazeTint(surfaceColor.copy(alpha = 0.35f)),
                                blurRadius = 16.dp
                            )
                        )
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    colorPrimary.copy(alpha = 0.75f),
                                    colorAccent.copy(alpha = 0.75f)
                                )
                            )
                        )
                        .widthIn(max = 260.dp)
                ) {
                    ExtendedFloatingActionButton(
                        onClick = onClick,
                        modifier = Modifier.widthIn(max = 260.dp),
                        containerColor = Color.Transparent,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                        icon = {
                            Animation(
                                id = R.raw.syme_bot,
                                modifier = Modifier.size(48.dp)
                            )
                        },
                        text = {
                            Text(
                                text = stringResource(id = R.string.bot_label),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorOnPrimary,
                                letterSpacing = 0.3.sp
                            )
                        }
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .shadow(10.dp, CircleShape)
                        .clip(CircleShape)
                        .hazeEffect(
                            state = hazeState,
                            style = HazeStyle(
                                backgroundColor = surfaceColor,
                                tint = HazeTint(surfaceColor.copy(alpha = 0.35f)),
                                blurRadius = 16.dp
                            )
                        )
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    colorPrimary.copy(alpha = 0.50f),
                                    colorAccent.copy(alpha = 0.50f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    FloatingActionButton(
                        onClick = onClick,
                        shape = CircleShape,
                        containerColor = Color.Transparent,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                        modifier = Modifier.size(62.dp)
                    ) {
                        Animation(
                            id = R.raw.syme_bot,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    text: String,
    colorBubbleBg: Color,
    colorBubbleText: Color,
    colorPrimary: Color,
    colorAccent: Color,
    hazeState: HazeState,
    surfaceColor: Color
) {
    val bubbleShape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = 18.dp,
        bottomEnd = 5.dp
    )

    Box {
        Box(
            modifier = Modifier
                .widthIn(max = 210.dp)
                .shadow(8.dp, bubbleShape)
                .clip(bubbleShape)
                .hazeEffect(
                    state = hazeState,
                    style = HazeStyle(
                        backgroundColor = surfaceColor,
                        tint = HazeTint(colorBubbleBg.copy(alpha = 0.45f)),
                        blurRadius = 16.dp
                    )
                )
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.White.copy(alpha = 0.08f),
                            0.6f to Color.Transparent
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                Brush.radialGradient(listOf(colorAccent, colorPrimary)),
                                CircleShape
                            )
                    )
                    Text(
                        text = "SYME Bot",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorAccent,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = text,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorBubbleText,
                    lineHeight = 18.sp
                )
            }
        }

        // ── Petite queue de bulle ─────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 6.dp, y = 4.dp)
                .size(width = 10.dp, height = 8.dp)
                .background(
                    colorBubbleBg.copy(alpha = 0.45f),
                    RoundedCornerShape(bottomEnd = 4.dp)
                )
        )
    }
}