package com.syme.ui.component.card

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.R
import com.syme.domain.model.Measurement
import com.syme.domain.model.Relay
import com.syme.ui.component.gaugemeter.GaugeMeterCard
import com.syme.ui.component.oiswitch.RelaySwitchRow
import com.syme.ui.component.text.Title
import kotlin.math.roundToInt

@Composable
fun MeterCard(
    meterId: String,
    measurement: Measurement,
    relays: List<Relay>,
    onRelayToggle: (Relay, Boolean) -> Unit,
    pricePerWh: Double = 0.25,
    moneyUnit: String = stringResource(id = R.string.currency_unit)
) {
    val powerActive = measurement.activePowerW ?: 0.0
    val energyActive = measurement.energyActiveWh ?: 0.0
    val voltage = measurement.voltage ?: 0.0
    val current = measurement.current ?: 0.0
    val amountToPay = energyActive * pricePerWh

    Column(
        modifier = Modifier.padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Meter ID: $meterId",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.width(24.dp))

        // ───── LIGNE HAUTE : Power & Energy ─────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            GaugeMeterCard(
                title = stringResource(id = R.string.power),
                value = powerActive.toFloat(),
                unit = stringResource(id = R.string.unit_watt),
                min = 0f,
                max = 5000f,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            GaugeMeterCard(
                title = stringResource(id = R.string.energy),
                value = energyActive.toFloat(),
                unit = stringResource(id = R.string.unit_wh),
                min = 0f,
                max = 10000f,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ───── LIGNE BASSE : Voltage & Current ─────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            GaugeMeterCard(
                title = stringResource(id = R.string.voltage),
                value = voltage.toFloat(),
                unit = stringResource(id = R.string.unit_v),
                min = 0f,
                max = 440f,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            GaugeMeterCard(
                title = stringResource(id = R.string.current),
                value = current.toFloat(),
                unit = stringResource(id = R.string.unit_a),
                min = 0f,
                max = 50f,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ───── MONTANT ─────
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.amount_to_pay),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${amountToPay.roundToInt()} $moneyUnit",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ───── RELAYS ─────
        RelaySwitchRow(
            relays = relays,
            onRelayToggle = onRelayToggle
        )
    }

}

@Preview(showBackground = true)
@Composable
fun MeterCardPreview() {
    val fakeRelays = listOf(
        Relay(relayId = "1", channel = 1, currentState = "ON"),
        Relay(relayId = "2", channel = 2, currentState = "OFF"),
        Relay(relayId = "3", channel = 3, currentState = "OFF"),
        Relay(relayId = "4", channel = 4, currentState = "ON")
    )

    MeterCard(
        meterId = "1",
        measurement = Measurement(
            voltage = 230.0,
            current = 5.2,
            activePowerW = 1200.0,
            reactivePowerVar = 400.0,
            energyActiveWh = 3400.0,
            energyReactiveVarh = 1200.0
        ),
        relays = fakeRelays,
        onRelayToggle = { relay, newState ->
            println("Relay ${relay.channel} -> ${if (newState) "ON" else "OFF"}")
        }
    )
}
