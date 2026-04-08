package com.syme.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.R
import com.syme.ui.component.compositionlocal.LocalCurrentUserSession

@Composable
fun HomeHeader(
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    unreadCount: Int = 0
) {
    // Couleurs provenant du thème
    val colorPrimary   = MaterialTheme.colorScheme.primary
    val colorAccent    = MaterialTheme.colorScheme.secondary
    val colorBgLight   = MaterialTheme.colorScheme.primaryContainer
    val colorError     = MaterialTheme.colorScheme.error
    val colorOnPrimary = MaterialTheme.colorScheme.onPrimary

    val currentUser = LocalCurrentUserSession.current
    val initials = listOfNotNull(currentUser?.firstName, currentUser?.lastName)
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.trim().first().toString() }
        .uppercase()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 48.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Left: brand + greeting ────────────────────────────────────────
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colorPrimary,
                letterSpacing = 1.5.sp
            )
        }

        // ── Right: notification + avatar ──────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Notification button
            Box {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(colorBgLight)
                        .clickable { onNotificationsClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = colorPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.TopEnd)
                            .clip(CircleShape)
                            .background(colorError),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = unreadCount.coerceAtMost(99).toString(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorOnPrimary
                        )
                    }
                }
            }

            // Avatar
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(colorPrimary, colorAccent))
                    )
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials.ifBlank { "?" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = colorOnPrimary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeHeaderPreview() {
    MaterialTheme {
        HomeHeader(unreadCount = 5)
    }
}