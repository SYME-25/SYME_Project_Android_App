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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.syme.domain.mapper.imageResId
import com.syme.domain.mapper.labelResId
import com.syme.domain.model.Appliance
import com.syme.domain.model.enumeration.ApplianceType
import com.syme.ui.component.text.EntityBadge

@Composable
fun ApplianceCard(
    item: Appliance,
    onClick: () -> Unit,
    contentAction: (@Composable () -> Unit)? = null
) {
    // Image background tinted with primary at low opacity — gives identity without being loud
    val imageBg = MaterialTheme.colorScheme.primaryContainer

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
            // ── BODY ──────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Image with tinted background
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(imageBg),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = item.type.imageResId,
                        contentDescription = null,
                        modifier = Modifier
                            .size(44.dp)
                            .padding(2.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                // Text block
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    // ID + pill on same line
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.applianceId,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            maxLines = 1,
                            letterSpacing = 0.3.sp
                        )
                        EntityBadge(
                            text = if (item.isSmart)
                                stringResource(id = com.syme.R.string.appliance_smart)
                            else
                                stringResource(id = com.syme.R.string.appliance_standard),
                            color = if (item.isSmart)
                                MaterialTheme.colorScheme.secondary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Name — main line
                    Text(
                        text = item.name.ifBlank { item.applianceId },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Type
                    Text(
                        text = stringResource(id = item.type.labelResId),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1
                    )
                }
            }

            // ── FOOTER ────────────────────────────────────────────────────────
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
                if (contentAction != null) {
                    contentAction()
                } else {
                    // Wattage if available
                    if (item.powerWatt > 0f) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = stringResource(
                                    id = com.syme.R.string.home_appliance_power,
                                    item.powerWatt
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(0.dp))
                    }
                    Text(
                        text = if (item.usageHoursPerDay > 0f)
                            stringResource(
                                id = com.syme.R.string.appliance_usage_hours,
                                item.usageHoursPerDay
                            )
                        else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
            }
        }
    }
}

@Composable
fun ApplianceRow(
    items: List<Appliance>,
    onClick: (Appliance) -> Unit,
    contentAction: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 110.dp)
    ) {
        if (items.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                strokeWidth = 2.dp
            )
        } else {
            LazyRow(contentPadding = PaddingValues(horizontal = 10.dp)) {
                items(items) { item ->
                    val descRes = item.metadata?.get("description") as? Int
                    val description = descRes?.let { stringResource(id = it) }
                    ApplianceCard(
                        item = item,
                        onClick = { onClick(item) },
                        contentAction = if (contentAction != null || description != null) {
                            {
                                if (contentAction != null) {
                                    EntityBadge(
                                        text = stringResource(
                                            id = com.syme.R.string.home_appliance_power,
                                            item.powerWatt
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else if (!description.isNullOrBlank()) {
                                    Text(
                                        text = description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        } else null
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ApplianceCardPreview() {
    val item = Appliance(
        applianceId = "1",
        name = "Réfrigérateur",
        type = ApplianceType.REFRIGERATOR
    )

    ApplianceCard(item, onClick = {}, contentAction = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "Description de l'appareil selon son type, juste pour tester l'affichage.",
                color = Color.Black.copy(alpha = 0.5f),
                fontSize = 8.sp
            )
        }
    })
}