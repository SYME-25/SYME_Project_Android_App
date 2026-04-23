package com.syme.ui.screen.installation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syme.R
import com.syme.domain.mapper.labelResId
import com.syme.domain.model.Installation
import com.syme.domain.model.Location
import com.syme.domain.model.enumeration.InstallationType
import com.syme.ui.component.actionbutton.AppButton
import com.syme.ui.component.actionbutton.AppSwitchWithLocationPermission
import com.syme.ui.component.compositionlocal.LocalCurrentUserSession
import com.syme.ui.component.field.NameField
import com.syme.ui.component.field.NumberField
import com.syme.ui.theme.SYMETheme
import com.syme.utils.buildTraceability
import com.syme.utils.generateId

@Composable
fun InstallationForm(
    item: Installation,
    isEditMode: Boolean,
    onSaveInstallation: (Installation) -> Unit
) {
    val owner = LocalCurrentUserSession.current
    var name            by remember { mutableStateOf("") }
    var nameError       by remember { mutableStateOf("") }
    var address         by remember { mutableStateOf("") }
    var powerSubscribed by remember { mutableStateOf(item.powerSubscribed.toString()) }
    var currentInstallation by remember { mutableStateOf(item) }

    // ── NOUVEAU : état du dialog de confirmation ─────────────────────────
    var showConfirmDialog    by remember { mutableStateOf(false) }
    var pendingInstallation  by remember { mutableStateOf<Installation?>(null) }

    val nameErrorMsg = stringResource(R.string.installation_name_error)
    val codeEntity   = stringResource(item.type.labelResId).take(1)
    val buttonText   = if (isEditMode)
        stringResource(R.string.dialog_edit_installation_title)
    else
        stringResource(R.string.installation_create_button)

    name = if (isEditMode) item.name else ""
    address = if (isEditMode) item.address else ""

    LaunchedEffect(owner?.userId) {
        val ownerId = owner?.userId
        if (!ownerId.isNullOrBlank()) {
            currentInstallation = currentInstallation.copy(
                trace = buildTraceability(null, ownerId)
            )
        }
    }

    // ── Dialog de confirmation ───────────────────────────────────────────
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    text = if (isEditMode)
                        stringResource(R.string.dialog_edit_installation_title)
                    else
                        stringResource(R.string.installation_create_button),
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isEditMode)
                        stringResource(R.string.dialog_confirm_edit_message)   // "Voulez-vous modifier cette installation ?"
                    else
                        stringResource(R.string.dialog_confirm_create_message), // "Voulez-vous créer cette installation ?"
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        pendingInstallation?.let { onSaveInstallation(it) }
                    }
                ) {
                    Text(stringResource(R.string.dialog_confirm)) // "Confirmer"
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel)) // "Annuler"
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Header ───────────────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(item.type.labelResId),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.home_installation_electrical),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // ── Description ───────────────────────────────────────────────────────
        val descRes = item.metadata?.get("description") as? Int
        if (descRes != null) {
            Text(
                text = stringResource(descRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
        )

        // ── Fields ────────────────────────────────────────────────────────────
        NameField(
            value = name,
            onValueChange = { name = it; nameError = "" },
            label = stringResource(R.string.installation_name_label),
            icon = { Icon(Icons.Outlined.Home, contentDescription = null) },
            error = nameError
        )
        NameField(
            value = address,
            onValueChange = { address = it },
            label = stringResource(R.string.installation_address_label),
            icon = { Icon(Icons.Outlined.LocationOn, contentDescription = null) },
            error = ""
        )
        NumberField(
            value = powerSubscribed,
            onValueChange = { powerSubscribed = it },
            label = stringResource(R.string.home_installation_power_subscribed_label),
            error = ""
        )

        // ── Location card ─────────────────────────────────────────────────────
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(R.string.installation_location_card_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = stringResource(R.string.installation_location_card_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                AppSwitchWithLocationPermission(
                    label = stringResource(R.string.installation_use_my_location),
                    onLocationStateChanged = { isEnabled, location ->
                        currentInstallation = currentInstallation.copy(
                            location = if (isEnabled && location != null) location else Location()
                        )
                    }
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    NameField(
                        value = currentInstallation.location.latitude.toString(),
                        onValueChange = {},
                        label = stringResource(R.string.installation_latitude),
                        icon = { Icon(Icons.Outlined.LocationOn, null) },
                        error = "",
                        modifier = Modifier.weight(1f)
                    )
                    NameField(
                        value = currentInstallation.location.longitude.toString(),
                        onValueChange = {},
                        label = stringResource(R.string.installation_longitude),
                        icon = { Icon(Icons.Outlined.LocationOn, null) },
                        error = "",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ── CTA ───────────────────────────────────────────────────────────
        AppButton(
            text     = buttonText,
            onClick  = {
                if (name.isBlank()) {
                    nameError = nameErrorMsg
                } else {
                    // Prépare l'objet et ouvre le dialog — NE sauvegarde PAS encore
                    pendingInstallation = currentInstallation.copy(
                        ownerId         = owner?.userId ?: "",
                        installationId  = if (isEditMode) item.installationId
                        else generateId("I", codeEntity),
                        name            = name,
                        powerSubscribed = powerSubscribed.toDoubleOrNull() ?: 10.0,
                        address         = address,
                        trace           = currentInstallation.trace.copy(active = isEditMode)
                    )
                    showConfirmDialog = true  // ← ouvre le dialog
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InstallationFormPreview() {
    SYMETheme {
        InstallationForm(
            item = Installation(
                type = InstallationType.RESIDENTIAL,
                metadata = mapOf("description" to R.string.installation_not_ready_message)
            ),
            isEditMode = false,
            onSaveInstallation = {}
        )
    }
}
