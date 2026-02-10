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
import com.syme.domain.model.Circuit
import com.syme.domain.model.enumeration.CircuitType
import com.syme.ui.component.text.TextWithBackground
import com.syme.ui.component.text.Title

@Composable
fun CircuitCard(
    item: Circuit,
    onClick: () -> Unit
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
                    model = R.drawable.power_socket,
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
                text = if (item.isProtected) stringResource(id = R.string.home_circuit_protected) else stringResource(id = R.string.home_circuit_unprotected),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Column() {
                Title(title = stringResource(id = R.string.relay_channel, item.relayChannel.toString()), fontSize = 12)

                Spacer(modifier = Modifier.height(4.dp))

                TextWithBackground(
                    text = stringResource(
                        id = R.string.circuit_state,
                        item.currentState
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun CircuitRow(
    items: List<Circuit>,
    onClick: (Circuit) -> Unit,
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
                    CircuitCard(
                        item = item,
                        onClick = { onClick(item) }
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
        circuitId = "1",
        name = "Réfrigérateur",
    )

    CircuitCard(item, onClick = {})
}