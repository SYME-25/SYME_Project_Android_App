package com.syme.ui.screen.bot.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.domain.model.ChatMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageBubble(message: ChatMessage) {
    val sdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) +
                slideInVertically(
                    animationSpec = tween(300),
                    initialOffsetY = { it / 4 }
                )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
        ) {
            // ── Avatar IA ────────────────────────────────────────────────────
            if (!message.isUser) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⚡", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // ── Colonne du message ───────────────────────────────────────────
            Column(
                modifier = if (message.isUser) {
                    // Les bulles user gardent une largeur max fixe
                    Modifier.widthIn(max = 340.dp)
                } else {
                    // Les bulles IA prennent tout l'espace disponible
                    // pour que les tableaux puissent s'étendre
                    Modifier.weight(1f)
                },
                horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
            ) {

                // ── Message USER ─────────────────────────────────────────────
                if (message.isUser) {
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 4.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 18.dp
                        ),
                        color = when {
                            message.isError -> MaterialTheme.colorScheme.errorContainer
                            else            -> MaterialTheme.colorScheme.primary
                        },
                        tonalElevation = 2.dp
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            val textColor = when {
                                message.isError -> MaterialTheme.colorScheme.onErrorContainer
                                else            -> MaterialTheme.colorScheme.onPrimary
                            }
                            when {
                                message.audioUri != null -> {
                                    AudioMessageBubble(
                                        isUserMessage   = true,
                                        durationSeconds = message.audioDurationSec,
                                        onPlayPause     = {}
                                    )
                                }
                                else -> {
                                    MarkdownText(
                                        raw      = message.content,
                                        color    = textColor,
                                        modifier = Modifier.widthIn(max = 300.dp)
                                    )
                                }
                            }
                        }
                    }

                    // ── Message IA ───────────────────────────────────────────────
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp, bottom = 2.dp)
                    ) {
                        when {
                            message.isLoading -> {
                                TypingIndicator()
                            }

                            message.isError -> {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.errorContainer
                                ) {
                                    Box(
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical   = 8.dp
                                        )
                                    ) {
                                        MarkdownText(
                                            raw      = message.content,
                                            color    = MaterialTheme.colorScheme.onErrorContainer,
                                            // Pas de widthIn fixe : laisse le contenu respirer
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }

                            message.audioUri != null -> {
                                AudioMessageBubble(
                                    isUserMessage   = false,
                                    durationSeconds = message.audioDurationSec,
                                    onPlayPause     = {}
                                )
                            }

                            else -> {
                                MarkdownText(
                                    raw      = message.content,
                                    color    = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth()   // plus de wrapContentWidth unbounded ici
                                )
                            }
                        }
                    }
                }

                // ── Timestamp ────────────────────────────────────────────────
                if (!message.isLoading) {
                    Text(
                        text  = sdf.format(Date(message.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            // ── Avatar User ──────────────────────────────────────────────────
            if (message.isUser) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Default.Person,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}