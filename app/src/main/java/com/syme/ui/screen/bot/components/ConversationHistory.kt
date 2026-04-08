package com.syme.ui.screen.bot.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syme.R
import com.syme.domain.model.Conversation
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ConversationHistory(
    conversations: List<Conversation>,
    currentConvId: String,
    onSelect: (String) -> Unit,
    onDelete: (String) -> Unit,
    onNewConv: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(tonalElevation = 4.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment    = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.history_title),
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                FilledTonalIconButton(onClick = onNewConv) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.action_new_conversation))
                }
            }
        }

        LazyColumn(
            modifier      = Modifier.weight(1f),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(
                conversations.sortedByDescending { it.timestamp },
                key = { it.id }
            ) { conv ->
                ConversationItem(
                    conversation = conv,
                    isSelected   = conv.id == currentConvId,
                    onClick      = { onSelect(conv.id) },
                    onDelete     = { onDelete(conv.id) },
                    dateFormat   = sdf
                )
            }
        }
    }
}
