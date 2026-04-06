package com.syme.ui.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.syme.domain.mapper.labelResId
import com.syme.domain.model.Meter
import com.syme.domain.model.enumeration.MeterStatus
import com.syme.ui.component.text.EntityBadge
import com.syme.ui.theme.SemanticError500
import com.syme.ui.theme.SemanticSuccess500

@Composable
fun MeterListItemCard(
    item: Meter,
    onClick: () -> Unit,
    contentAction: (@Composable () -> Unit)? = null
) {
    val isActive = item.status.name == "ACTIVE"
    val stateColor = if (isActive) SemanticSuccess500 else SemanticError500
    val imageBg = stateColor.copy(alpha = 0.09f)

    Card(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .width(280.dp),
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
                        model = R.drawable.electric_meter,
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
                            text = item.installationId,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            letterSpacing = 0.3.sp,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        EntityBadge(
                            text = stringResource(item.status.labelResId),
                            color = stateColor
                        )
                    }

                    Text(
                        text = item.meterId.ifBlank {
                            stringResource(R.string.meter_no_installation)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = stringResource(id = item.meterType.labelResId),
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
                        text = if (isActive)
                            stringResource(R.string.meter_connected)
                        else
                            stringResource(R.string.meter_disconnected),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                }
                Text(
                    text = stringResource(
                        R.string.meter_relay_count,
                        item.relays.size
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        }
    }
}

@Composable
fun MeterListItemRow(items: List<Meter>, onClick: (Meter) -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().heightIn(min = 110.dp)) {
        if (items.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                strokeWidth = 2.dp
            )
        } else {
            LazyRow(contentPadding = PaddingValues(horizontal = 10.dp)) {
                items(items) { item ->
                    MeterListItemCard(item = item, onClick = { onClick(item) })
                }
            }
        }
    }
}

@Preview
@Composable
fun MeterListItemPreview() {
    val item = Meter(
        meterId = "1",
        status = MeterStatus.ACTIVE,
    )

    MeterListItemCard(item, onClick = {}, contentAction = {})
}