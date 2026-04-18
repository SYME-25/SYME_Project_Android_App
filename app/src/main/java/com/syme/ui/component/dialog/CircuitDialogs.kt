package com.syme.ui.component.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syme.R
import com.syme.domain.model.Circuit
import com.syme.domain.model.Meter
import com.syme.ui.component.actionbutton.AppButton
import com.syme.ui.component.actionbutton.AppSwitch
import com.syme.ui.component.actionbutton.AppTextButton
import com.syme.ui.component.field.DropdownField
import com.syme.ui.component.field.NameField
import com.syme.ui.component.field.NumberField

// ─────────────────────────────────────────────────────────────────────────────
// EDIT
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CircuitEditDialog(
    circuit: Circuit,
    meters: List<Meter>,
    onDismiss: () -> Unit,
    onConfirm: (Circuit) -> Unit
) {
    var name            by remember { mutableStateOf(circuit.name) }
    var selectedMeterId by remember { mutableStateOf(circuit.meterId) }
    var relayChannel    by remember { mutableStateOf(circuit.relayChannel?.toString() ?: "") }
    var priority        by remember { mutableStateOf(circuit.priority.toString()) }
    var isProtected     by remember { mutableStateOf(circuit.isProtected) }

    val relayChannelInt = relayChannel.toIntOrNull()
    val priorityInt     = priority.toIntOrNull()

    val isValid = name.isNotBlank()
            && relayChannelInt != null && relayChannelInt > 0
            && priorityInt     != null && priorityInt >= 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.dialog_edit_circuit_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = circuit.name.ifBlank { circuit.circuitId.toString() },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // ── Read-only ID ───────────────────────────────────────────────
                NameField(
                    value = circuit.circuitId.toString(),
                    onValueChange = {},
                    label = stringResource(R.string.label_circuit_id),
                    error = ""
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )

                // ── Nom ────────────────────────────────────────────────────────
                NameField(
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(R.string.label_circuit_name),
                    error = if (name.isBlank())
                        stringResource(R.string.error_field_required) else ""
                )

                // ── Compteur associé ───────────────────────────────────────────
                if (meters.isNotEmpty()) {
                    DropdownField(
                        value = selectedMeterId,
                        onValueChange = { selectedMeterId = it },
                        label = stringResource(R.string.label_meter),
                        error = "",
                        items = meters.map { it.meterId }
                    )
                }

                // ── Canal relais ───────────────────────────────────────────────
                NumberField(
                    value = relayChannel,
                    onValueChange = { relayChannel = it },
                    label = stringResource(R.string.label_relay_channel),
                    error = if (relayChannelInt == null || relayChannelInt <= 0)
                        stringResource(R.string.error_positive_integer) else ""
                )

                // ── Priorité ───────────────────────────────────────────────────
                NumberField(
                    value = priority,
                    onValueChange = { priority = it },
                    label = stringResource(R.string.label_priority),
                    error = if (priorityInt == null || priorityInt < 0)
                        stringResource(R.string.error_positive_number) else ""
                )

                // ── Circuit protégé ────────────────────────────────────────────
                AppSwitch(
                    checked = isProtected,
                    onCheckedChange = { isProtected = it },
                    label = stringResource(R.string.label_is_protected),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        },
        confirmButton = {
            AppButton(
                text = stringResource(R.string.action_save),
                onClick = {
                    onConfirm(
                        circuit.copy(
                            meterId      = selectedMeterId,
                            relayChannel = relayChannelInt ?: circuit.relayChannel,
                            name         = name.trim(),
                            priority     = priorityInt     ?: circuit.priority,
                            isProtected  = isProtected
                        )
                    )
                },
                enabled = isValid
            )
        },
        dismissButton = {
            AppTextButton(
                text = stringResource(R.string.action_cancel),
                onClick = onDismiss,
                textColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// DELETE
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CircuitDeleteDialog(
    circuit: Circuit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Outlined.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = stringResource(R.string.dialog_delete_circuit_title),
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = stringResource(
                    R.string.dialog_delete_circuit_message,
                    circuit.name.ifBlank { circuit.circuitId.toString() }
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            AppButton(
                text = stringResource(R.string.action_delete),
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            )
        },
        dismissButton = {
            AppTextButton(
                text = stringResource(R.string.action_cancel),
                onClick = onDismiss,
                textColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}
