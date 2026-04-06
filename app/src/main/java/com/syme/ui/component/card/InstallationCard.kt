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
import com.syme.R
import com.syme.domain.mapper.imageResId
import com.syme.domain.mapper.labelResId
import com.syme.domain.model.Installation
import com.syme.domain.model.enumeration.InstallationType
import com.syme.ui.component.text.EntityBadge
import com.syme.ui.theme.SemanticError500
import com.syme.ui.theme.SemanticSuccess500

@Composable
fun InstallationCard(
    item: Installation,
    onClick: () -> Unit,
    contentAction: (@Composable () -> Unit)? = null
) {
    val isActive = item.trace.active
    val stateColor = if (isActive) SemanticSuccess500 else SemanticError500
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
                        model = item.type.imageResId,
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
                            maxLines = 1
                        )
                        EntityBadge(
                            text = if (isActive)
                                stringResource(R.string.installation_state_on)
                            else
                                stringResource(R.string.installation_state_off),
                            color = stateColor
                        )
                    }

                    Text(
                        text = item.name.ifBlank { item.installationId },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = stringResource(id = item.type.labelResId),
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
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(stateColor)
                    )
                    Text(
                        text = item.address.ifBlank {
                            stringResource(R.string.installation_no_address)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (contentAction != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    contentAction()
                } else if (item.energyWh > 0.0) {
                    Text(
                        text = stringResource(
                            R.string.home_installation_energy,
                            item.energyWh / 1000.0
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
            }
        }
    }
}

@Composable
fun InstallationRow(
    items: List<Installation>,
    onClick: (Installation) -> Unit,
    contentAction: (@Composable () -> Unit)? = null
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
                    InstallationCard(
                        item = item,
                        onClick = { onClick(item) },
                        contentAction = contentAction
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun InstallationCardPreview(){
    val item = Installation(
        installationId = "1",
        name = "Yohann",
        type = InstallationType.RESIDENTIAL
    )

    InstallationCard(item, onClick = {}, contentAction = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "Description de l'installation, selon le type d'installation, j'ajoute du texte juste pour voir jusqu'où ça peu aller",
                color = Color.Black.copy(alpha = 0.5f),
                fontSize = 8.sp
            )
        }
    })
}
