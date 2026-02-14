package com.syme.ui.component.oiswitch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syme.domain.model.Relay

@Composable
fun RelaySwitchRow(
    relays: List<Relay>,
    onRelayToggle: (Relay, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ───── TITRE CENTRÉ ─────
            Text(
                text = "Relays",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ───── LISTE HORIZONTALE ─────
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(relays) { relay ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CH ${relay.channel}",
                            style = MaterialTheme.typography.labelMedium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        OiSwitch(
                            isOn = relay.currentState == "ON",
                            onToggle = { newState ->
                                onRelayToggle(relay, newState)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RelaySwitchRowPreview() {
    val relays = listOf(
        Relay(relayId = "1", channel = 1, currentState = "ON"),
        Relay(relayId = "2", channel = 2, currentState = "OFF"),
        Relay(relayId = "3", channel = 3, currentState = "OFF"),
        Relay(relayId = "4", channel = 4, currentState = "ON"),
        Relay(relayId = "5", channel = 5, currentState = "OFF")
    )

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            RelaySwitchRow(
                relays = relays,
                onRelayToggle = { relay, newState ->
                    println("Relay ${relay.channel} -> ${if (newState) "ON" else "OFF"}")
                }
            )
        }
    }
}
