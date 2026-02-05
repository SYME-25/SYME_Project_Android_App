package com.syme.ui.screen.installation

import android.R.attr.verticalDivider
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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.syme.domain.mapper.labelResId
import com.syme.domain.model.Installation
import com.syme.domain.model.enumeration.InstallationType
import com.syme.ui.component.actionbutton.AppButton
import com.syme.ui.component.compositionlocal.LocalCurrentUserSession
import com.syme.ui.component.field.NameField
import com.syme.utils.generateId

@Composable
fun InstallationDetailBody(
    item: Installation,
    onSaveInstallation: (Installation) -> Unit
){

    val ownerId = LocalCurrentUserSession.current?.userId

    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var addressError by remember { mutableStateOf("") }

    val nameErrorMsg = stringResource(R.string.installation_name_error)

    val codeEntity = stringResource(item.type.labelResId).drop(1)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(item.type.labelResId) + " " + stringResource(R.string.home_installation_electrical),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(item.metadata?.get("description") as Int),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(24.dp))

        // ✏️ NOM (obligatoire)
        NameField(
            value = name,
            onValueChange = {
                name = it
                if (nameError.isNotEmpty()) nameError = ""
            },
            label = stringResource(R.string.installation_name_label),
            icon = {
                Icon(Icons.Outlined.Home, contentDescription = null)
            },
            error = nameError
        )

        NameField(
            value = address,
            onValueChange = {
                address = it
                if (addressError.isNotEmpty()) addressError = ""
            },
            label = stringResource(R.string.installation_address_label),
            icon = {
                Icon(Icons.Outlined.LocationOn, contentDescription = null)
            },
            error = addressError
        )

        Spacer(Modifier.height(24.dp))

        // ⚠️ ETAT
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.width(  8.dp))
            Text(
                text = stringResource(R.string.installation_not_ready_message),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(32.dp))

        // ✅ ACTIONS
        AppButton(
            text = stringResource(R.string.installation_create_button),
            onClick = {
                if (name.isBlank()) {
                    nameError = nameErrorMsg
                } else {
                    onSaveInstallation(
                        item.copy(
                            ownerId = ownerId ?: "",
                            installationId = generateId("I", codeEntity),
                            name = name,
                            address = address,
                            trace = item.trace.copy(active = false)
                        )
                    )
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InstallationDetailBodyPreview() {
    InstallationDetailBody(
        item = Installation(
            name = "My Installation",
            type = InstallationType.RESIDENTIAL,
            address = "123 Main St, Anytown, USA"
        ),
        onSaveInstallation = {}
    )
}