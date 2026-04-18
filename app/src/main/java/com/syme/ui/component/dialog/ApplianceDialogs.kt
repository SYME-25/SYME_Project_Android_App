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
import com.syme.domain.model.Appliance
import com.syme.ui.component.actionbutton.AppButton
import com.syme.ui.component.actionbutton.AppSwitch
import com.syme.ui.component.actionbutton.AppTextButton
import com.syme.ui.component.field.NameField
import com.syme.ui.component.field.NumberField

// ─────────────────────────────────────────────────────────────────────────────
// EDIT
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ApplianceEditDialog(
    appliance: Appliance,
    onDismiss: () -> Unit,
    onConfirm: (Appliance) -> Unit
) {
    var isSmart     by remember { mutableStateOf(appliance.isSmart) }
    var powerWatt   by remember { mutableStateOf(appliance.powerWatt.toString()) }
    var powerFactor by remember { mutableStateOf(appliance.powerFactor.toString()) }
    var usageHours  by remember { mutableStateOf(appliance.usageHoursPerDay.toString()) }

    val powerWattFloat   = powerWatt.toFloatOrNull()
    val powerFactorFloat = powerFactor.toFloatOrNull()
    val usageHoursFloat  = usageHours.toFloatOrNull()

    val isValid = powerWattFloat   != null && powerWattFloat   >= 0f
            && powerFactorFloat != null && powerFactorFloat in 0f..1f
            && usageHoursFloat  != null && usageHoursFloat  in 0f..24f

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.dialog_edit_appliance_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = appliance.name.ifBlank { appliance.applianceId },
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
                    value = appliance.applianceId,
                    onValueChange = {},
                    label = stringResource(R.string.label_appliance_id),
                    error = ""
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )

                // ── isSmart ────────────────────────────────────────────────────
                AppSwitch(
                    checked = isSmart,
                    onCheckedChange = { isSmart = it },
                    label = stringResource(R.string.label_is_smart),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                // ── Puissance nominale ─────────────────────────────────────────
                NumberField(
                    value = powerWatt,
                    onValueChange = { powerWatt = it },
                    label = stringResource(R.string.label_power_watt),
                    error = if (powerWattFloat == null || powerWattFloat < 0f)
                        stringResource(R.string.error_positive_number) else ""
                )

                // ── Facteur de puissance ───────────────────────────────────────
                NumberField(
                    value = powerFactor,
                    onValueChange = { powerFactor = it },
                    label = stringResource(R.string.label_power_factor),
                    error = if (powerFactorFloat == null || powerFactorFloat !in 0f..1f)
                        stringResource(R.string.error_between_0_and_1) else ""
                )

                // ── Heures d'utilisation ───────────────────────────────────────
                NumberField(
                    value = usageHours,
                    onValueChange = { usageHours = it },
                    label = stringResource(R.string.label_usage_hours),
                    error = if (usageHoursFloat == null || usageHoursFloat !in 0f..24f)
                        stringResource(R.string.error_between_0_and_24) else ""
                )
            }
        },
        confirmButton = {
            AppButton(
                text = stringResource(R.string.action_save),
                onClick = {
                    onConfirm(
                        appliance.copy(
                            isSmart          = isSmart,
                            powerWatt        = powerWattFloat   ?: appliance.powerWatt,
                            powerFactor      = powerFactorFloat ?: appliance.powerFactor,
                            usageHoursPerDay = usageHoursFloat  ?: appliance.usageHoursPerDay
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
fun ApplianceDeleteDialog(
    appliance: Appliance,
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
                text = stringResource(R.string.dialog_delete_appliance_title),
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = stringResource(
                    R.string.dialog_delete_appliance_message,
                    appliance.name.ifBlank { appliance.applianceId }
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
