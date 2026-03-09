package com.syme.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.R
import com.syme.ui.component.compositionlocal.LocalCurrentUserSession
import com.syme.ui.component.text.Title
import com.syme.utils.TimeUtils

@Composable
fun HomeHeader(
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    unreadCount: Int = 0
){
    val currentUser = LocalCurrentUserSession.current

    val initials = listOfNotNull(currentUser?.firstName, currentUser?.lastName)
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.trim().first().toString() }
        .uppercase()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 16.dp, top = 40.dp),
        verticalAlignment = Alignment.CenterVertically
    ){

        // 🟦 Partie gauche (titre)
        Title(
            title = "SYME",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 30,
            modifier = Modifier.weight(1f)
        )

        // 🟨 Partie droite (icônes)
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            NotificationIcon(
                unreadCount = unreadCount,
                onClick = onNotificationsClick
            )

            Box(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Red)
                    .clickable { onProfileClick() },
            ) {
                Text(
                    text = initials,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun NotificationIcon(unreadCount: Int, onClick: () -> Unit) {
    BadgedBox(
        modifier = Modifier, // tu peux aussi jouer ici si besoin
        badge = {
            if (unreadCount > 0) {
                Badge(
                    modifier = Modifier
                        .size(20.dp)
                        .offset(x = (-10).dp, y = 10.dp), // ← rapproche le badge
                    containerColor = Color.Red,       // ← fond rouge
                    contentColor = Color.White        // ← texte blanc
                ) {
                    Text(
                        text = unreadCount.coerceAtMost(99).toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Preview
@Composable
fun HomeHeaderPreview(){
    HomeHeader(unreadCount = 5)
}