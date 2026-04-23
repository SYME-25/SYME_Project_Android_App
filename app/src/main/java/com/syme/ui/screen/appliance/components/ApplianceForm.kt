package com.syme.ui.screen.appliance.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syme.R
import com.syme.domain.mapper.labelResId
import com.syme.domain.model.Appliance
import com.syme.domain.model.Circuit
import com.syme.domain.model.enumeration.ApplianceType
import com.syme.ui.component.actionbutton.AppButton
import com.syme.ui.component.compositionlocal.LocalCurrentUserSession
import com.syme.ui.component.field.*
import com.syme.utils.buildTraceability
import com.syme.utils.generateId

@Composable
fun ApplianceForm(
    item: Appliance,
    circuits: List<Circuit>,
    isEditMode: Boolean,
    onSaveAppliance: (Appliance) -> Unit
) {
    val owner = LocalCurrentUserSession.current

    var currentAppliance by remember { mutableStateOf(item) }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingAppliances by remember { mutableStateOf<List<Appliance>>(emptyList()) }

    // 🔥 ALIGNÉ AVEC INSTALLATION
    LaunchedEffect(owner?.userId) {
        val ownerId = owner?.userId
        if (!ownerId.isNullOrBlank()) {
            currentAppliance = currentAppliance.copy(
                trace = buildTraceability(
                    existing = if (isEditMode) currentAppliance.trace else null,
                    currentUserId = ownerId
                )
            )
        }
    }

    val initialCircuitName = circuits.find {
        it.circuitId.toString() == item.circuitId
    }?.name ?: circuits.firstOrNull()?.name.orEmpty()

    var selectedCircuit by remember { mutableStateOf(initialCircuitName) }
    var powerWatt by remember { mutableStateOf(if (isEditMode) item.powerWatt.toString() else "") }
    var powerFactor by remember { mutableStateOf(if (isEditMode) item.powerFactor.toString() else "") }
    var usageHours by remember { mutableStateOf(if (isEditMode) item.usageHoursPerDay.toString() else "") }
    var quantity by remember { mutableStateOf("1") }

    fun parseFloat(v: String) = v.replace(',', '.').toFloatOrNull() ?: 0f

    val buttonText = if (isEditMode)
        stringResource(R.string.dialog_edit_appliance_title)
    else
        stringResource(R.string.appliance_create_button)

    // Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(buttonText, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    if (isEditMode)
                        stringResource(R.string.dialog_confirm_edit_message)
                    else
                        stringResource(R.string.dialog_confirm_create_message)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    pendingAppliances.forEach { onSaveAppliance(it) }
                }) {
                    Text(stringResource(R.string.dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = stringResource(item.type.labelResId),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )

        DropdownField(
            value = selectedCircuit,
            onValueChange = { selectedCircuit = it },
            label = stringResource(R.string.appliance_circuit_label),
            error = "",
            items = circuits.map { it.name }
        )

        NumberField(
            value = powerWatt,
            onValueChange = { powerWatt = it },
            label = stringResource(R.string.appliance_power_label),
            error = ""
        )

        NumberField(
            value = powerFactor,
            onValueChange = { powerFactor = it },
            label = stringResource(R.string.appliance_power_factor_label),
            error = ""
        )

        NumberField(
            value = usageHours,
            onValueChange = { usageHours = it },
            label = stringResource(R.string.appliance_usage_hours_label),
            error = ""
        )

        if (!isEditMode) {
            NumberField(
                value = quantity,
                onValueChange = { quantity = it },
                label = stringResource(R.string.appliance_quantity_label),
                error = ""
            )
        }

        AppButton(
            text = buttonText,
            onClick = {
                val circuitId = circuits.first { it.name == selectedCircuit }.circuitId

                pendingAppliances = if (isEditMode) {
                    listOf(
                        currentAppliance.copy(
                            circuitId = circuitId.toString(),
                            powerWatt = parseFloat(powerWatt),
                            powerFactor = parseFloat(powerFactor),
                            usageHoursPerDay = parseFloat(usageHours),
                            trace = buildTraceability(
                                existing = currentAppliance.trace,
                                currentUserId = owner?.userId ?: ""
                            )
                        )
                    )
                } else {
                    (1..(quantity.toIntOrNull() ?: 1)).map { index ->
                        currentAppliance.copy(
                            applianceId = generateId("A", selectedCircuit.take(1) + index),
                            circuitId = circuitId.toString(),
                            powerWatt = parseFloat(powerWatt),
                            powerFactor = parseFloat(powerFactor),
                            usageHoursPerDay = parseFloat(usageHours),
                            trace = buildTraceability(
                                existing = null,
                                currentUserId = owner?.userId ?: ""
                            )
                        )
                    }
                }

                showConfirmDialog = true
            }
        )
    }
}