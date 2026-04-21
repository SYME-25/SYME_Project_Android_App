package com.syme.ui.screen.bot.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.syme.R
import com.syme.ui.component.text.Title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotTopBar(
    onOpenHistory: () -> Unit,
    onNewConversation: () -> Unit,
    onBackClick: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Title(
                title = stringResource(R.string.bot_title),
                onBackClick = onBackClick
                )
        },
        navigationIcon = {
            IconButton(onClick = onOpenHistory) {
                Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.cd_history))
            }
        },
        actions = {
            IconButton(onClick = onNewConversation) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.action_new_conversation))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}
