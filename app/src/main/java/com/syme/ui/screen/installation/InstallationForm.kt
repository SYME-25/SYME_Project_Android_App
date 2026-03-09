package com.syme.ui.screen.installation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
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
    onSaveInstallation: (Installation) -> Unit
) {
    val owner = LocalCurrentUserSession.current

    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var addressError by remember { mutableStateOf("") }
    var powerSubscribed by remember { mutableStateOf("10") }

    // 🌍 Etat local de l'installation
    var currentInstallation by remember { mutableStateOf(item) }

    // ✅ Etat réel du switch
    var useLocation by remember { mutableStateOf(false) }

    val nameErrorMsg = stringResource(R.string.installation_name_error)
    val codeEntity = stringResource(item.type.labelResId).take(1)

    LaunchedEffect(owner?.userId) {
        val ownerId = owner?.userId
        if (!ownerId.isNullOrBlank()) {
            currentInstallation = currentInstallation.copy(
                trace = buildTraceability(null, ownerId)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(item.type.labelResId) + " " +
                    stringResource(R.string.home_installation_electrical),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(item.metadata?.get("description") as Int),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(24.dp))

        NameField(
            value = name,
            onValueChange = {
                name = it
                nameError = ""
            },
            label = stringResource(R.string.installation_name_label),
            icon = { Icon(Icons.Outlined.Home, contentDescription = null) },
            error = nameError
        )

        NameField(
            value = address,
            onValueChange = {
                address = it
                addressError = ""
            },
            label = stringResource(R.string.installation_address_label),
            icon = { Icon(Icons.Outlined.LocationOn, contentDescription = null) },
            error = addressError
        )

        NumberField(
            value = powerSubscribed,
            onValueChange = {
                powerSubscribed = it
            },
            label = stringResource(R.string.home_installation_power_subscribed_label),
            error = ""
        )

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.installation_location_card_title),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(text = stringResource(R.string.installation_location_card_description))

                Spacer(Modifier.height(12.dp))

                AppSwitchWithLocationPermission(
                    label = stringResource(R.string.installation_use_my_location),
                    onLocationStateChanged = { isEnabled, location ->
                        if (isEnabled && location != null) {
                            currentInstallation = currentInstallation.copy(location = location)
                        } else {
                            currentInstallation = currentInstallation.copy(location = Location())
                        }
                    }
                )

                Spacer(Modifier.height(12.dp))

                NameField(
                    value = currentInstallation.location?.latitude?.toString() ?: "",
                    onValueChange = {},
                    label = stringResource(R.string.installation_latitude),
                    icon = { Icon(Icons.Outlined.LocationOn, null) },
                    error = ""
                )

                Spacer(Modifier.height(8.dp))

                NameField(
                    value = currentInstallation.location?.longitude?.toString() ?: "",
                    onValueChange = {},
                    label = stringResource(R.string.installation_longitude),
                    icon = { Icon(Icons.Outlined.LocationOn, null) },
                    error = ""
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        AppButton(
            text = stringResource(R.string.installation_create_button),
            onClick = {
                if (name.isBlank()) {
                    nameError = nameErrorMsg
                } else {
                    onSaveInstallation(
                        currentInstallation.copy(
                            ownerId = owner?.userId ?: "",
                            installationId = generateId("I", codeEntity),
                            name = name,
                            powerSubscribed = powerSubscribed.toDouble(),
                            energyWh = 0.0,
                            address = address,
                            trace = currentInstallation.trace.copy(active = false)
                        )
                    )
                }
            }
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
            onSaveInstallation = {}
        )
    }
}
