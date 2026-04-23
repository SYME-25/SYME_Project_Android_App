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
    onSaveAppliance: (Appliance) -> Unit
) {
    val ownerId = LocalCurrentUserSession.current?.userId

    var selectedCircuit  by remember { mutableStateOf(circuits.firstOrNull()?.name ?: "") }
    var circuitError     by remember { mutableStateOf("") }
    var powerWatt        by remember { mutableStateOf("") }
    var powerError       by remember { mutableStateOf("") }
    var powerFactor      by remember { mutableStateOf("") }
    var factorError      by remember { mutableStateOf("") }
    var quantity         by remember { mutableStateOf("1") }
    var quantityError    by remember { mutableStateOf("") }
    var usageHours       by remember { mutableStateOf("") }
    var usageHoursError  by remember { mutableStateOf("") }
    var otherName        by remember { mutableStateOf("") }
    var otherDescription by remember { mutableStateOf("") }
    var otherError       by remember { mutableStateOf("") }

    val selectedType = item.type
    val circuitNames = circuits.map { it.name }

    fun parseFloat(input: String) = input.replace(',', '.').toFloatOrNull() ?: 0f

    val energyWh = remember(powerWatt, powerFactor, usageHours) {
        parseFloat(powerWatt) * parseFloat(powerFactor) * parseFloat(usageHours)
    }

    // Strings erreurs
    val circuitErrorMsg   = stringResource(R.string.home_appliance_circuit_error)
    val powerErrorMsg     = stringResource(R.string.home_appliance_power_error)
    val factorErrorMsg    = stringResource(R.string.home_appliance_power_factor_error)
    val quantityErrorMsg  = stringResource(R.string.home_appliance_quantity_error)
    val usageHoursErrorMsg = stringResource(R.string.home_appliance_usage_hours_error)
    val otherErrorMsg     = stringResource(R.string.home_appliance_other_error)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // ── Titre de l'appareil ───────────────────────────────────
        Text(
            text = stringResource(item.type.labelResId),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(R.string.home_add_appliance_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(4.dp))

        // ── Description ───────────────────────────────────────────
        val descRes = item.metadata?.get("description") as? Int
        if (descRes != null) {
            Text(
                text = stringResource(descRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
        )

        // ── Champs dans une carte arrondie ────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                )
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DropdownField(
                value = selectedCircuit,
                onValueChange = { selectedCircuit = it; circuitError = "" },
                label = stringResource(R.string.appliance_circuit_label),
                error = circuitError,
                items = circuitNames
            )
            NumberField(
                value = powerWatt,
                onValueChange = { powerWatt = it; powerError = "" },
                label = stringResource(R.string.appliance_power_label),
                error = powerError
            )
            NumberField(
                value = powerFactor,
                onValueChange = { powerFactor = it; factorError = "" },
                label = stringResource(R.string.appliance_power_factor_label),
                error = factorError
            )
            NumberField(
                value = usageHours,
                onValueChange = { usageHours = it; usageHoursError = "" },
                label = stringResource(R.string.appliance_usage_hours_label),
                error = usageHoursError
            )
            NumberField(
                value = quantity,
                onValueChange = { quantity = it; quantityError = "" },
                label = stringResource(R.string.appliance_quantity_label),
                error = quantityError
            )

            if (selectedType == ApplianceType.OTHER) {
                NameField(
                    value = otherName,
                    onValueChange = { otherName = it },
                    label = stringResource(R.string.appliance_other_name_label),
                    error = otherError
                )
                TextAreaField(
                    value = otherDescription,
                    onValueChange = { otherDescription = it },
                    label = stringResource(R.string.appliance_other_description_label)
                )
            }
        }

        // ── Estimation énergie ────────────────────────────────────
        if (energyWh > 0f) {
            Spacer(Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(
                        R.string.appliance_energy_estimation,
                        String.format("%.2f", energyWh)
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Bouton ────────────────────────────────────────────────
        AppButton(
            text = stringResource(R.string.appliance_create_button),
            onClick = {
                var hasError = false
                val power  = parseFloat(powerWatt)
                val factor = parseFloat(powerFactor)
                val hours  = parseFloat(usageHours)

                if (selectedCircuit.isBlank()) { circuitError = circuitErrorMsg; hasError = true }
                if (power <= 0f)              { powerError = powerErrorMsg;     hasError = true }
                if (factor <= 0f)             { factorError = factorErrorMsg;   hasError = true }
                if (quantity.toIntOrNull() == null || quantity.toInt() < 1) {
                    quantityError = quantityErrorMsg; hasError = true
                }
                if (hours !in 0f..24f)        { usageHoursError = usageHoursErrorMsg; hasError = true }
                if (selectedType == ApplianceType.OTHER &&
                    (otherName.isBlank() || otherDescription.isBlank())
                ) { otherError = otherErrorMsg; hasError = true }

                if (!hasError) {
                    val circuitId = circuits.first { it.name == selectedCircuit }.circuitId
                    repeat(quantity.toInt()) { index ->
                        onSaveAppliance(
                            item.copy(
                                applianceId = generateId(
                                    "A",
                                    selectedCircuit.take(1) + (index + 1)
                                ),
                                circuitId = circuitId.toString(),
                                powerWatt = power,
                                powerFactor = factor,
                                usageHoursPerDay = hours,
                                metadata = if (selectedType == ApplianceType.OTHER)
                                    mapOf("name" to otherName, "description" to otherDescription)
                                else item.metadata,
                                trace = buildTraceability(null, ownerId ?: "", isActive = true)
                            )
                        )
                    }
                }
            }
        )

        Spacer(Modifier.height(8.dp))
    }
}