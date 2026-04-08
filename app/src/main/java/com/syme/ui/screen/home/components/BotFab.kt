package com.syme.ui.screen.home

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.R
import com.syme.ui.component.animation.Animation
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
fun BotFab(onClick: () -> Unit) {
    val colorPrimary   = MaterialTheme.colorScheme.primary
    val colorAccent    = MaterialTheme.colorScheme.secondary
    val colorBubbleBg  = MaterialTheme.colorScheme.surfaceVariant
    val colorBubbleText = MaterialTheme.colorScheme.onSurfaceVariant
    val colorOnPrimary = MaterialTheme.colorScheme.onPrimary

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
            ChatBubble(text = stringResource(id = BOT_MESSAGES[messageIndex]), colorBubbleBg, colorBubbleText, colorPrimary, colorAccent)
        }

        // ── FAB ───────────────────────────────────────────────────────────
        AnimatedContent(
            targetState = initialExpanded,
            transitionSpec = {
                fadeIn(tween(400)) togetherWith fadeOut(tween(300))
            },
            label = "BotFabTransition"
        ) { isExpanded ->
            if (isExpanded) {
                Box(
                    modifier = Modifier
                        .shadow(8.dp, RoundedCornerShape(32.dp))
                        .background(
                            Brush.horizontalGradient(listOf(colorPrimary, colorAccent)),
                            RoundedCornerShape(32.dp)
                        )
                        .widthIn(max = 260.dp)
                ) {
                    ExtendedFloatingActionButton(
                        onClick = onClick,
                        modifier = Modifier.widthIn(max = 260.dp),
                        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0f),
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
                        .background(
                            Brush.linearGradient(listOf(colorPrimary, colorAccent)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    FloatingActionButton(
                        onClick = onClick,
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0f),
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
    colorBubbleBg: androidx.compose.ui.graphics.Color,
    colorBubbleText: androidx.compose.ui.graphics.Color,
    colorPrimary: androidx.compose.ui.graphics.Color,
    colorAccent: androidx.compose.ui.graphics.Color
) {
    Box {
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = 18.dp,
                bottomEnd = 5.dp
            ),
            shadowElevation = 8.dp,
            color = colorBubbleBg,
            modifier = Modifier.widthIn(max = 210.dp)
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

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 6.dp, y = 4.dp)
                .size(width = 10.dp, height = 8.dp)
                .background(
                    colorBubbleBg,
                    RoundedCornerShape(bottomEnd = 4.dp)
                )
        )
    }
}