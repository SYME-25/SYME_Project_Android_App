package com.syme.ui.component.card

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.syme.R
import com.syme.domain.model.Circuit
import com.syme.domain.model.enumeration.CircuitState
import com.syme.ui.component.text.EntityBadge
import com.syme.ui.theme.SemanticError500
import com.syme.ui.theme.SemanticSuccess500

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CircuitCard(
    item: Circuit,
    relayState: String,
    onClick: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    val isOn = relayState != "OFF"
    val stateColor = if (isOn) SemanticSuccess500 else SemanticError500
    val imageBg = stateColor.copy(alpha = 0.10f)

    var menuExpanded by remember { mutableStateOf(false) }

    Box {
        Card(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 5.dp)
                .width(280.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        if (onEdit != null || onDelete != null) menuExpanded = true
                    }
                ),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(
                0.5.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(imageBg),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = R.drawable.power_socket,
                            contentDescription = null,
                            modifier = Modifier.size(44.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.relay_channel,
                                    item.relayChannel?.toString() ?: "-"
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                letterSpacing = 0.3.sp
                            )
                            EntityBadge(
                                text = stringResource(R.string.circuit_state, relayState),
                                color = stateColor
                            )
                        }
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (item.isProtected)
                                stringResource(R.string.home_circuit_protected)
                            else
                                stringResource(R.string.home_circuit_unprotected),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(stateColor)
                        )
                        Text(
                            text = if (isOn)
                                stringResource(R.string.circuit_footer_on)
                            else
                                stringResource(R.string.circuit_footer_off),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                        )
                    }
                    if (item.isProtected) {
                        Text(
                            text = stringResource(R.string.circuit_priority, item.priority),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                }
            }
        }

        // ── Context menu ──────────────────────────────────────────────────────
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            onEdit?.let { action ->
                DropdownMenuItem(
                    text = { Text(stringResource(id = com.syme.R.string.action_edit)) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Edit, contentDescription = null)
                    },
                    onClick = {
                        menuExpanded = false
                        action()
                    }
                )
            }
            onDelete?.let { action ->
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(id = com.syme.R.string.action_delete),
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        action()
                    }
                )
            }
        }
    }
}

@Composable
fun CircuitRow(
    items: List<Circuit>,
    onClick: (Circuit) -> Unit,
    onEdit: ((Circuit) -> Unit)? = null,
    onDelete: ((Circuit) -> Unit)? = null
) {
    Box(modifier = Modifier.fillMaxWidth().heightIn(min = 110.dp)) {
        if (items.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                strokeWidth = 2.dp
            )
        } else {
            LazyRow(contentPadding = PaddingValues(horizontal = 10.dp)) {
                items(items) { item ->
                    CircuitCard(
                        item = item,
                        relayState = item.currentState.name,
                        onClick = { onClick(item) },
                        onEdit = onEdit?.let { { it(item) } },
                        onDelete = onDelete?.let { { it(item) } }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun CircuitCardPreview() {
    val item = Circuit(
        circuitId = 1,
        name = "Climatisation très longue pour tester overflow",
        relayChannel = 1,
        currentState = CircuitState.ON,
        isProtected = true,
    )
    CircuitCard(
        item = item,
        relayState = "OFF",
        onClick = {},
        onEdit = {},
        onDelete = {}
    )
}
