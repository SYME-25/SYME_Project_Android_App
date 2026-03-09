package com.syme.ui.component.card

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.syme.ui.theme.switchGreen
import com.syme.ui.theme.switchRed
import kotlin.math.roundToInt

@Composable
fun MeterCard(
    meterId: String,
    meterState: String,
    measurement: Measurement,
    relays: List<Relay>,
    onRelayToggle: (Relay, Boolean) -> Unit,
    moneyUnit: String = stringResource(id = R.string.currency_unit)
) {
    val powerActive = measurement.activePowerW ?: 0.0
    val energyActive = measurement.energyActiveWh ?: 0.0
    val voltage = measurement.voltage ?: 0.0
    val current = measurement.current ?: 0.0

    val stateColor = if (meterState != "ACTIVE") {
        switchRed
    } else {
        switchGreen
    }

    LazyColumn(
        modifier = Modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item {
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
                Spacer(modifier = Modifier.width(6.dp))
                GaugeMeterCard(
                    title = stringResource(id = R.string.energy),
                    value = energyActive.toFloat(),
                    unit = stringResource(id = R.string.unit_wh),
                    min = 0f,
                    max = 7f,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(6.dp)) }

        item {
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
                Spacer(modifier = Modifier.width(6.dp))
                GaugeMeterCard(
                    title = stringResource(id = R.string.current),
                    value = current.toFloat(),
                    unit = stringResource(id = R.string.unit_a),
                    min = 0f,
                    max = 20f,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = stateColor.copy(alpha = 0.2f)
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.meter_id, meterId),
                        fontSize = 14.sp,
                        color = stateColor
                    )
                    Text(
                        text = stringResource(id = R.string.meter_state, meterState),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = stateColor
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            RelaySwitchRow(
                relays = relays,
                onRelayToggle = onRelayToggle
            )
        }

        item { Spacer(modifier = Modifier.height(6.dp)) }
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
        meterState = "ACTIVE",
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