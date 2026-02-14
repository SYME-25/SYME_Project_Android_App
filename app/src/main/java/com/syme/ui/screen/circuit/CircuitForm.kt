package com.syme.ui.screen.circuit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.R
import com.syme.domain.model.Meter
import com.syme.ui.component.actionbutton.AppButton
import com.syme.ui.component.actionbutton.AppCheckbox
import com.syme.ui.component.field.DropdownField
import com.syme.ui.component.field.NameField

@Composable
fun CircuitForm(
    meters: List<Meter>,
    onSave: (
        meterId: String,
        relayChannel: Int,
        name: String,
        priority: Int,
        isProtected: Boolean
    ) -> Unit
) {

    var selectedMeterId by remember { mutableStateOf("") }
    var relayChannel by remember { mutableStateOf<Int?>(null) }
    var name by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf<Int?>(null) }
    var isProtected by remember { mutableStateOf(false) }

    var meterError by remember { mutableStateOf("") }
    var relayError by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf("") }
    var priorityError by remember { mutableStateOf("") }

    val selectedMeter = meters.find { it.meterId == selectedMeterId }

    // 🔥 Relay dynamique basé sur le meter choisi
    val relayOptions = selectedMeter
        ?.relays
        ?.map { it.channel.toString() }
        ?: emptyList()

    val circuitErrorMeterRequiredMsg = stringResource(R.string.circuit_error_meter_required)
    val circuitErrorRelayRequiredMsg = stringResource(R.string.circuit_error_relay_required)
    val circuitErrorNameRequiredMsg = stringResource(R.string.circuit_error_name_required)
    val circuitErrorPriorityRequiredMsg = stringResource(R.string.circuit_error_priority_required)

    fun validate(): Boolean {
        meterError =
            if (selectedMeterId.isBlank())
                circuitErrorMeterRequiredMsg
            else ""

        relayError =
            if (relayChannel == null)
                circuitErrorRelayRequiredMsg
            else ""

        nameError =
            if (name.isBlank())
                circuitErrorNameRequiredMsg
            else ""

        priorityError =
            if (priority == null)
                circuitErrorPriorityRequiredMsg
            else ""

        return meterError.isEmpty() &&
                relayError.isEmpty() &&
                nameError.isEmpty() &&
                priorityError.isEmpty()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item {
            Text(
                text = stringResource(R.string.circuit_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }

        // 🧭 Sélection Meter
        item {
            DropdownField(
                value = selectedMeterId,
                onValueChange = {
                    selectedMeterId = it
                    relayChannel = null // reset relay quand meter change
                },
                label = stringResource(R.string.circuit_label_meter),
                error = meterError,
                items = meters.map { it.meterId }
            )
        }

        // ⚡ Sélection Relay (uniquement si meter choisi)
        item {
            DropdownField(
                value = relayChannel?.toString() ?: "",
                onValueChange = { relayChannel = it.toIntOrNull() },
                label = stringResource(R.string.circuit_label_relay),
                error = relayError,
                items = relayOptions
            )
        }

        // 🏷 Nom
        item {
            NameField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.circuit_label_name),
                error = nameError
            )
        }

        // 🎚 Priority
        item {
            DropdownField(
                value = priority?.toString() ?: "",
                onValueChange = { priority = it.toIntOrNull() },
                label = stringResource(R.string.circuit_label_priority),
                error = priorityError,
                items = (0..5).map { it.toString() }
            )
        }

        // 🛡 Protection
        item {
            AppCheckbox(
                checked = isProtected,
                onCheckedChange = { isProtected = it },
                label = stringResource(R.string.circuit_label_protected)
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // 💾 Save
        item {
            AppButton(
                text = stringResource(R.string.circuit_button_submit),
                onClick = {
                    if (validate()) {
                        onSave(
                            selectedMeterId,
                            relayChannel!!,
                            name.trim(),
                            priority!!,
                            isProtected
                        )
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CircuitFormPreview() {

    val fakeMeters = listOf(
        Meter(
            meterId = "Meter-001",
            relays = listOf(
                com.syme.domain.model.Relay(channel = 1),
                com.syme.domain.model.Relay(channel = 2),
                com.syme.domain.model.Relay(channel = 3)
            )
        )
    )

    CircuitForm(meters = fakeMeters) { _, _, _, _, _ -> }
}
