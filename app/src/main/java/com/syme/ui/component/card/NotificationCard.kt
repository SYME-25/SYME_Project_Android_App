package com.syme.ui.component.card

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.R
import com.syme.domain.model.Notification
import com.syme.domain.model.NotificationColors
import com.syme.domain.model.enumeration.NotificationCategory
import com.syme.domain.model.enumeration.NotificationType
import com.syme.ui.theme.*
import com.syme.utils.TimeUtils.toRelativeTime

// ── Couleurs par type ─────────────────────────────────────────────────────────

@Composable
private fun notificationTypeColors(type: NotificationType): NotificationColors =
    when (type) {
        NotificationType.CRITICAL -> NotificationColors(
            bar      = SemanticError500,
            iconTint = SemanticError500,
            iconBg   = SemanticError100,
            chipText = SemanticError500,
            chipBg   = SemanticError100,
        )
        NotificationType.WARNING -> NotificationColors(
            bar      = SemanticWarning500,
            iconTint = SemanticWarning500,
            iconBg   = SemanticWarning100,
            chipText = SemanticWarning500,
            chipBg   = SemanticWarning100,
        )
        NotificationType.SUCCESS -> NotificationColors(
            bar      = SemanticSuccess500,
            iconTint = SemanticSuccess500,
            iconBg   = SemanticSuccess100,
            chipText = SemanticSuccess500,
            chipBg   = SemanticSuccess100,
        )
        NotificationType.INFO -> NotificationColors(
            bar      = SemanticInfo500,
            iconTint = SemanticInfo500,
            iconBg   = SemanticInfo100,
            chipText = SemanticInfo500,
            chipBg   = SemanticInfo100,
        )
    }

// ── Card principale ───────────────────────────────────────────────────────────
@Composable
fun NotificationCard(
    notification: Notification,
    onClick: (Notification) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val colors = notificationTypeColors(notification.type)
    var expanded by remember { mutableStateOf(false) }

    val containerColor = if (!notification.isRead)
        MaterialTheme.colorScheme.surface
    else
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    val borderColor = if (!notification.isRead)
        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    else
        MaterialTheme.colorScheme.outlineVariant

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, borderColor),
        tonalElevation = 0.dp
    ) {
        Column {
            // ── Ligne principale ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .clickable {
                        expanded = !expanded
                        onClick(notification)
                    }
            ) {
                // Barre verticale — fillMaxHeight() fonctionne car le Row
                // n'a pas de verticalAlignment qui entre en conflit
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .padding(vertical = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
                            .background(colors.bar)
                    )
                }

                // Contenu — CenterVertically pour centrer l'icône
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 12.dp, bottom = 12.dp, start = 11.dp, end = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(11.dp),
                    verticalAlignment = Alignment.CenterVertically  // ← corrigé
                ) {
                    NotificationIcon(type = notification.type, colors = colors)

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = notification.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                                maxLines = 2
                            )
                            if (!notification.isRead) {
                                Spacer(Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(colors.bar)
                                )
                            }
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = notification.body,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = if (expanded) Int.MAX_VALUE else 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = notification.trace.createdAt.toRelativeTime(context),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                                NotificationCategoryChip(
                                    category = notification.category,
                                    colors = colors
                                )
                            }
                            val rotation by animateFloatAsState(
                                targetValue = if (expanded) 90f else 0f,
                                label = "chevron"
                            )
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .size(15.dp)
                                    .graphicsLayer { rotationZ = rotation }
                            )
                        }
                    }
                }
            }

            // ── Panneau de détail animé ───────────────────────────────────
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit  = shrinkVertically() + fadeOut()
            ) {
                NotificationDetailPanel(
                    notification = notification,
                    barColor = colors.bar
                )
            }
        }
    }
}

// ── Panneau de détail ─────────────────────────────────────────────────────────

@Composable
private fun NotificationDetailPanel(
    notification: Notification,
    barColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Column(
            modifier = Modifier.padding(
                start = 26.dp, end = 14.dp,
                top = 12.dp, bottom = 13.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Grille de métadonnées depuis notification.metadata
            if (notification.metadata.isNotEmpty()) {
                val entries = notification.metadata.entries.toList()
                val chunked = entries.chunked(2)
                chunked.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        row.forEach { (key, value) ->
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = key.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    letterSpacing = 0.05.sp
                                )
                                Text(
                                    text = value,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        // Padding si nombre impair
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            // Corps complet de la notif
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .then(
                        Modifier.padding(start = 0.dp) // la barre remplace le padding gauche
                    )
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .background(barColor)
                )
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

// ── Icon ──────────────────────────────────────────────────────────────────────

@Composable
fun NotificationIcon(
    type: NotificationType,
    colors: NotificationColors = notificationTypeColors(type)
) {
    val icon: ImageVector = when (type) {
        NotificationType.CRITICAL -> Icons.Default.Warning
        NotificationType.WARNING  -> Icons.Default.Info
        NotificationType.SUCCESS  -> Icons.Default.CheckCircle
        NotificationType.INFO     -> Icons.Default.Notifications
    }
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.iconBg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.iconTint,
            modifier = Modifier.size(17.dp)
        )
    }
}

// ── Category chip ─────────────────────────────────────────────────────────────

@Composable
fun NotificationCategoryChip(
    category: NotificationCategory,
    colors: NotificationColors? = null
) {
    val label = when (category) {
        NotificationCategory.POWER       -> stringResource(R.string.notification_power)
        NotificationCategory.BILL        -> stringResource(R.string.notification_bill)
        NotificationCategory.METER       -> stringResource(R.string.notification_meter)
        NotificationCategory.CONSUMPTION -> stringResource(R.string.notification_consumption)
        NotificationCategory.RELAY       -> stringResource(R.string.notification_relay)
        NotificationCategory.DEMAND      -> stringResource(R.string.notification_demand)
        NotificationCategory.SYSTEM      -> stringResource(R.string.notification_system)
    }
    val chipText = colors?.chipText ?: MaterialTheme.colorScheme.onSurfaceVariant
    val chipBg   = colors?.chipBg   ?: MaterialTheme.colorScheme.surfaceVariant

    Text(
        text = label,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(chipBg)
            .padding(horizontal = 7.dp, vertical = 2.dp),
        style = MaterialTheme.typography.labelSmall,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = chipText
    )
}