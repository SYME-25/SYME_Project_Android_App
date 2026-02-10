package com.syme.ui.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.syme.domain.model.Appliance
import com.syme.domain.model.enumeration.ApplianceType
import com.syme.ui.component.text.TextWithBackground

@Composable
fun ApplianceCard(
    item: Appliance,
    onClick: () -> Unit,
    contentAction: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .size(width = 190.dp, height = 270.dp),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(165.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = item.type.imageResId,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.name,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(id = item.type.labelResId),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (contentAction != null) {
                contentAction()
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
        Modifier
            .fillMaxWidth()
            .heightIn(min = 260.dp)
    ) {
        if (items.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) {
                items(items) { item ->

                    val descRes = item.metadata?.get("description") as? Int
                    val description = descRes?.let { stringResource(id = it) }

                    ApplianceCard(
                        item = item,
                        onClick = { onClick(item) },
                        contentAction = {
                            if (contentAction != null) {
                                TextWithBackground(
                                    text = stringResource(
                                        id = R.string.home_appliance_power,
                                        item.powerWatt
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                if (!description.isNullOrBlank()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = description,
                                            fontSize = 8.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
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