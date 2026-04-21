package com.syme.ui.screen.notification

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syme.domain.model.enumeration.NotificationCategory
import com.syme.domain.state.UiState
import com.syme.ui.component.card.NotificationCard
import com.syme.ui.component.compositionlocal.LocalCurrentUserSession
import com.syme.ui.viewmodel.NotificationsViewModel
import com.syme.utils.TimeUtils.toDayLabel
import com.syme.R
import com.syme.ui.component.filter.FilterSection
import com.syme.ui.component.text.Title

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
    contentPadding : PaddingValues,
    onBackClick: (() -> Unit)? = null
) {
    val context     = LocalContext.current
    val currentUser = LocalCurrentUserSession.current
    val userId      = currentUser?.userId ?: ""

    val uiState       by viewModel.state.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val selectedCat   by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val unreadCount   by viewModel.unreadCount.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) viewModel.observe(userId)
    }

    val categories = listOf(null) + NotificationCategory.entries

    // Groupes triés du plus ancien au plus récent (ordre WhatsApp)
    val grouped = remember(notifications) {
        notifications
            .sortedBy { it.trace.createdAt }           // plus ancien en premier
            .groupBy { it.trace.createdAt.toDayLabel(context) }  // grouper par jour
    }

    // Nombre total d'items dans la liste (headers + cards + spacers)
    val totalItemCount = remember(grouped) {
        grouped.values.sumOf { it.size + 2 } // +2 par groupe : header + spacer
    }

    // Scroll automatique vers le bas à l'ouverture et quand la liste change
    LaunchedEffect(totalItemCount) {
        if (totalItemCount > 0) {
            listState.scrollToItem(totalItemCount - 1)
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(contentPadding)
    ) {

        NotificationsHeader(
            unreadCount = unreadCount,
            onMarkAllRead = { viewModel.markAllAsRead(userId) },
            onBackClick = onBackClick
        )

        FilterSection(
            title = stringResource(R.string.notification_filter_type),
            items = NotificationCategory.entries,
            selectedItem = selectedCat,
            onItemSelected = { viewModel.setFilter(it) },
            itemLabel = { stringResource(it.displayName) },
            allLabel = stringResource(R.string.all)
        )

        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

        when (uiState) {
            is UiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = (uiState as UiState.Error).message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            else -> {
                if (notifications.isEmpty()) {
                    NotificationsEmptyState()
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(
                            start = 12.dp,
                            top = 8.dp,
                            end = 12.dp,
                            bottom = 24.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        grouped.forEach { (day, items) ->
                            stickyHeader(key = "header_$day") {
                                NotificationsDayHeader(day = day)
                            }
                            items(items, key = { it.notificationId }) { notif ->
                                NotificationCard(
                                    notification = notif,
                                    onClick = { viewModel.markAsRead(userId, notif) }
                                )
                            }
                            item(key = "spacer_$day") {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun NotificationsHeader(
    unreadCount: Int,
    onMarkAllRead: () -> Unit,
    onBackClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 8.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Title(
            title = stringResource(R.string.notifications_title),
            padding = 0,
            modifier = Modifier.weight(1f),
            onBackClick = onBackClick
        )
        if (unreadCount > 0) {
            IconButton(onClick = onMarkAllRead) {
                Icon(
                    imageVector = Icons.Outlined.DoneAll,
                    contentDescription = stringResource(R.string.mark_all_read),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ── Day header ────────────────────────────────────────────────────────────────

@Composable
private fun NotificationsDayHeader(day: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 16.dp, bottom = 8.dp, start = 4.dp, end = 4.dp)
    ) {
        Text(
            text = day,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun NotificationsEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.no_notifications),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.no_notifications_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

// ── Extension ─────────────────────────────────────────────────────────────────

val NotificationCategory.displayName: Int
    get() = when (this) {
        NotificationCategory.POWER       -> R.string.notification_power
        NotificationCategory.BILL        -> R.string.notification_bill
        NotificationCategory.METER       -> R.string.notification_meter
        NotificationCategory.CONSUMPTION -> R.string.notification_consumption
        NotificationCategory.RELAY       -> R.string.notification_relay
        NotificationCategory.DEMAND      -> R.string.notification_demand
        NotificationCategory.SYSTEM      -> R.string.notification_system
    }