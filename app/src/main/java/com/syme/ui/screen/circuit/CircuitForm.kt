package com.syme.ui.screen.circuit

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.R
import com.syme.ui.component.actionbutton.AppButton
import com.syme.ui.component.actionbutton.AppCheckbox
import com.syme.ui.component.field.DropdownField
import com.syme.ui.component.field.NameField

@Composable
fun CircuitForm(
    meterList: List<String>,
    onSubmit: (meterId: String, relayChannel: Int?, name: String, priority: Int, isProtected: Boolean) -> Unit
) {
    var selectedMeter by remember { mutableStateOf("") }
    var relayChannel by remember { mutableStateOf<Int?>(null) }
    var name by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf<Int?>(null) }
    var isProtected by remember { mutableStateOf(false) }

    var meterError by remember { mutableStateOf("") }
    var relayError by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf("") }
    var priorityError by remember { mutableStateOf("") }

    val circuitErrorMeterMsg = stringResource(R.string.circuit_error_meter_required)
    val circuitErrorRelayMsg = stringResource(R.string.circuit_error_relay_required)
    val circuitErrorNameMsg = stringResource(R.string.circuit_error_name_required)
    val circuitErrorPriorityMsg = stringResource(R.string.circuit_error_priority_required)

    fun validate(): Boolean {
        meterError = if (selectedMeter.isBlank()) circuitErrorMeterMsg else ""
        relayError = if (relayChannel == null) circuitErrorRelayMsg else ""
        nameError = if (name.isBlank()) circuitErrorNameMsg else ""
        priorityError = if (priority == null) circuitErrorPriorityMsg else ""

        return meterError.isEmpty() && relayError.isEmpty() && nameError.isEmpty() && priorityError.isEmpty()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = stringResource(R.string.circuit_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )
        }

        item {
            DropdownField(
                value = selectedMeter,
                onValueChange = { selectedMeter = it },
                label = stringResource(R.string.circuit_label_meter),
                error = meterError,
                items = meterList
            )
        }

        item {
            DropdownField(
                value = relayChannel?.toString() ?: "",
                onValueChange = { relayChannel = it.toIntOrNull() },
                label = stringResource(R.string.circuit_label_relay),
                error = relayError,
                items = (1..4).map { it.toString() } // exemple de channels 1-4
            )
        }

        item {
            NameField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.circuit_label_name),
                error = nameError
            )
        }

        item {
            DropdownField(
                value = priority?.toString() ?: "",
                onValueChange = { priority = it.toIntOrNull() },
                label = stringResource(R.string.circuit_label_priority),
                error = priorityError,
                items = (0..5).map { it.toString() } // Priorities 0-5
            )
        }

        item {
            AppCheckbox(
                checked = isProtected,
                onCheckedChange = { isProtected = it },
                label = stringResource(R.string.circuit_label_protected)
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            AppButton(
                text = stringResource(R.string.circuit_button_submit),
                onClick = {
                    if (validate()) {
                        onSubmit(selectedMeter, relayChannel, name, priority!!, isProtected)
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CircuitFormPreview() {
    val meters = listOf("Meter 1", "Meter 2", "Meter 3")
    CircuitForm(meterList = meters) { meterId, relay, name, priority, isProtected ->
        // handle submit
    }
}
