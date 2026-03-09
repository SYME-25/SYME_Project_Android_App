package com.syme.ui.screen.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.R
import com.syme.ui.component.animation.Animation
import com.syme.ui.component.text.Title
import kotlinx.coroutines.delay

private val BOT_MESSAGES = listOf(
    R.string.bot_message_1,  // Need help?
    R.string.bot_message_2,  // Got a question?
    R.string.bot_message_3,  // Let’s talk!
    R.string.bot_message_4,  // Your usage today?
    R.string.bot_message_6,  // Usage peak detected
    R.string.bot_message_5,  // Possible savings!
    R.string.bot_message_8,  // Your estimated bill?
    R.string.bot_message_9,  // Compare your data?
    R.string.bot_message_7,  // Let’s optimize together!
    R.string.bot_message_10  // Energy tip 💡
)

@Composable
fun BotFab(onClick: () -> Unit) {
    var initialExpanded by remember { mutableStateOf(true) }
    var bubbleVisible by remember { mutableStateOf(false) }
    var messageIndex by remember { mutableIntStateOf(0) }

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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Bulle message à gauche du FAB (uniquement après rétractation)
        AnimatedVisibility(
            visible = !initialExpanded && bubbleVisible,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(400)
            ) + fadeIn(tween(400)),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeOut(tween(300))
        ) {
            ChatBubble(text = stringResource(id = BOT_MESSAGES[messageIndex]))
        }

        // Switch entre ExtendedFAB (phase initiale) et FAB simple (état stable)
        AnimatedContent(
            targetState = initialExpanded,
            transitionSpec = {
                fadeIn(animationSpec = tween(400)) togetherWith
                        fadeOut(animationSpec = tween(300))
            },
            label = "BotFabTransition"
        ) { isExpanded ->
            if (isExpanded) {
                ExtendedFloatingActionButton(
                    onClick = onClick,
                    modifier = Modifier.widthIn(max = 260.dp),
                    icon = {
                        Animation(
                            id = R.raw.syme_bot,
                            modifier = Modifier.size(48.dp)
                        )
                    },
                    text = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.bot_label),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            } else {
                FloatingActionButton(
                    onClick = onClick,
                    shape = CircleShape,
                ) {
                    Animation(
                        id = R.raw.syme_bot,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(text: String) {
    Surface(
        shape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 16.dp,
            bottomEnd = 4.dp // petite pointe vers le FAB à droite
        ),
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = text,
            modifier = Modifier
                .widthIn(max = 200.dp)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}