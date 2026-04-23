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
import com.syme.domain.model.Installation
import com.syme.domain.model.Location
import com.syme.ui.component.actionbutton.AppButton
import com.syme.ui.component.actionbutton.AppSwitch
import com.syme.ui.component.actionbutton.AppSwitchWithLocationPermission
import com.syme.ui.component.actionbutton.AppTextButton
import com.syme.ui.component.field.NameField
import com.syme.ui.component.field.NumberField

// ─────────────────────────────────────────────────────────────────────────────
// EDIT
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun InstallationEditDialog(
    installation: Installation,
    onDismiss: () -> Unit,
    onConfirm: (Installation) -> Unit
) {
    var name            by remember { mutableStateOf(installation.name) }
    var address         by remember { mutableStateOf(installation.address) }
    var powerSubscribed by remember { mutableStateOf(installation.powerSubscribed.toString()) }
    var energyWh        by remember { mutableStateOf(installation.energyWh.toString()) }
    var hasSolarPanels  by remember { mutableStateOf(installation.hasSolarPanels) }
    var solarPowerKw    by remember { mutableStateOf(installation.solarPowerKw.toString()) }

    // 🌍 Nouvelle gestion de localisation
    var currentLocation by remember { mutableStateOf(installation.location) }

    val powerDouble  = powerSubscribed.toDoubleOrNull()
    val energyDouble = energyWh.toDoubleOrNull()
    val solarDouble  = solarPowerKw.toDoubleOrNull()

    val isValid = name.isNotBlank()
            && powerDouble  != null && powerDouble  >= 0.0
            && energyDouble != null && energyDouble >= 0.0
            && (!hasSolarPanels || (solarDouble != null && solarDouble >= 0.0))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.dialog_edit_installation_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = installation.name.ifBlank { installation.installationId },
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
                    value = installation.installationId,
                    onValueChange = {},
                    label = stringResource(R.string.label_installation_id),
                    error = ""
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )

                // ── Nom ────────────────────────────────────────────────────────
                NameField(
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(R.string.label_name),
                    error = if (name.isBlank())
                        stringResource(R.string.error_field_required) else ""
                )

                // ── Adresse ────────────────────────────────────────────────────
                NameField(
                    value = address,
                    onValueChange = { address = it },
                    label = stringResource(R.string.label_address),
                    error = ""
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )

                // ── Localisation (UX améliorée) ────────────────────────────────
                Text(
                    text = stringResource(R.string.label_location),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                AppSwitchWithLocationPermission(
                    label = stringResource(R.string.installation_use_my_location),
                    onLocationStateChanged = { isEnabled, location ->
                        currentLocation = if (isEnabled && location != null) {
                            location
                        } else {
                            installation.location ?: Location()
                        }
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    NameField(
                        value = currentLocation.latitude.toString(),
                        onValueChange = {},
                        label = stringResource(R.string.label_latitude),
                        error = "",
                        modifier = Modifier.weight(1f)
                    )
                    NameField(
                        value = currentLocation.longitude.toString(),
                        onValueChange = {},
                        label = stringResource(R.string.label_longitude),
                        error = "",
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )

                // ── Puissance souscrite ────────────────────────────────────────
                NumberField(
                    value = powerSubscribed,
                    onValueChange = { powerSubscribed = it },
                    label = stringResource(R.string.label_power_subscribed),
                    error = if (powerDouble == null || powerDouble < 0.0)
                        stringResource(R.string.error_positive_number) else ""
                )

                // ── Énergie journalière ────────────────────────────────────────
                NumberField(
                    value = energyWh,
                    onValueChange = { energyWh = it },
                    label = stringResource(R.string.label_energy_wh),
                    error = if (energyDouble == null || energyDouble < 0.0)
                        stringResource(R.string.error_positive_number) else ""
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )

                // ── Panneaux solaires ──────────────────────────────────────────
                AppSwitch(
                    checked = hasSolarPanels,
                    onCheckedChange = { hasSolarPanels = it },
                    label = stringResource(R.string.label_has_solar_panels),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                // ── Puissance solaire (conditionnelle) ─────────────────────────
                if (hasSolarPanels) {
                    NumberField(
                        value = solarPowerKw,
                        onValueChange = { solarPowerKw = it },
                        label = stringResource(R.string.label_solar_power_kw),
                        error = if (solarDouble == null || solarDouble < 0.0)
                            stringResource(R.string.error_positive_number) else ""
                    )
                }
            }
        },
        confirmButton = {
            AppButton(
                text = stringResource(R.string.action_save),
                onClick = {
                    onConfirm(
                        installation.copy(
                            name            = name.trim(),
                            address         = address.trim(),
                            location        = currentLocation,
                            powerSubscribed = powerDouble  ?: installation.powerSubscribed,
                            energyWh        = energyDouble ?: installation.energyWh,
                            hasSolarPanels  = hasSolarPanels,
                            solarPowerKw    = if (hasSolarPanels)
                                solarDouble ?: installation.solarPowerKw
                            else 0.0
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
fun InstallationDeleteDialog(
    installation: Installation,
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
                text = stringResource(R.string.dialog_delete_installation_title),
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(
                        R.string.dialog_delete_installation_message,
                        installation.name.ifBlank { installation.installationId }
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = stringResource(R.string.dialog_delete_installation_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
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