package com.syme.ui.screen.appliance

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.R
import com.syme.domain.mapper.labelResId
import com.syme.domain.model.Appliance
import com.syme.domain.model.Circuit
import com.syme.domain.model.enumeration.ApplianceType
import com.syme.ui.component.actionbutton.AppButton
import com.syme.ui.component.compositionlocal.LocalCurrentUserSession
import com.syme.ui.component.field.DropdownField
import com.syme.ui.component.field.NumberField
import com.syme.ui.component.field.NameField
import com.syme.ui.component.field.TextAreaField
import com.syme.utils.buildTraceability
import com.syme.utils.generateId

@Composable
fun ApplianceDetailBody(
    item: Appliance,
    circuits: List<Circuit>,
    onSaveAppliance: (Appliance) -> Unit
) {
    val ownerId = LocalCurrentUserSession.current?.userId

    // 🔹 Circuits statiques temporaires
    val circuits = remember {
        listOf(
            Circuit(circuitId = "C1", name = "Kitchen Lighting"),
            Circuit(circuitId = "C2", name = "Living Room AC"),
            Circuit(circuitId = "C3", name = "Bedroom Heater")
        )
    }

    var selectedCircuit by remember { mutableStateOf(circuits.firstOrNull()?.name ?: "") }
    var circuitError by remember { mutableStateOf("") }

    var powerWatt by remember { mutableStateOf("") }
    var powerError by remember { mutableStateOf("") }

    var powerFactor by remember { mutableStateOf("") }
    var factorError by remember { mutableStateOf("") }

    var quantity by remember { mutableStateOf("1") }
    var quantityError by remember { mutableStateOf("") }

    var usageHours by remember { mutableStateOf("") }
    var usageHoursError by remember { mutableStateOf("") }

    val selectedType = item.type

    var otherName by remember { mutableStateOf("") }
    var otherDescription by remember { mutableStateOf("") }
    var otherError by remember { mutableStateOf("") }

    val circuitNames = circuits.map { it.name }

    // 🌍 Conversion locale-safe
    fun parseFloat(input: String): Float =
        input.replace(',', '.').toFloatOrNull() ?: 0f

    val energyWh = remember(powerWatt, powerFactor, usageHours) {
        parseFloat(powerWatt) * parseFloat(powerFactor) * parseFloat(usageHours)
    }

    val circuitErrorMsg = stringResource(R.string.home_appliance_circuit_error)
    val powerErrorMsg = stringResource(R.string.home_appliance_power_error)
    val factorErrorMsg = stringResource(R.string.home_appliance_power_factor_error)
    val quantityErrorMsg = stringResource(R.string.home_appliance_quantity_error)
    val usageHoursErrorMsg = stringResource(R.string.home_appliance_usage_hours_error)
    val otherErrorMsg = stringResource(R.string.home_appliance_other_error)

    // Build traceability
    LaunchedEffect(item?.applianceId) {
        val ownerId = ownerId
        if (!ownerId.isNullOrBlank()) {
            item.copy(
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
            text = stringResource(item.type.labelResId),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(item.metadata?.get("description") as Int),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(16.dp))

        // 🔌 CIRCUIT
        DropdownField(
            value = selectedCircuit,
            onValueChange = {
                selectedCircuit = it
                circuitError = ""
            },
            label = stringResource(R.string.appliance_circuit_label),
            error = circuitError,
            items = circuitNames
        )

        // ⚡ POWER
        NumberField(
            value = powerWatt,
            onValueChange = {
                powerWatt = it
                powerError = ""
            },
            label = stringResource(R.string.appliance_power_label),
            error = powerError
        )

        // 📐 POWER FACTOR
        NumberField(
            value = powerFactor,
            onValueChange = {
                powerFactor = it
                factorError = ""
            },
            label = stringResource(R.string.appliance_power_factor_label),
            error = factorError
        )

        // ⏱ USAGE HOURS
        NumberField(
            value = usageHours,
            onValueChange = {
                usageHours = it
                usageHoursError = ""
            },
            label = stringResource(R.string.appliance_usage_hours_label),
            error = usageHoursError
        )

        // 🔢 QUANTITY
        NumberField(
            value = quantity,
            onValueChange = {
                quantity = it
                quantityError = ""
            },
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

        if (energyWh > 0f) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(
                    R.string.appliance_energy_estimation,
                    String.format("%.2f", energyWh)
                ),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(32.dp))

        AppButton(
            text = stringResource(R.string.appliance_create_button),
            onClick = {
                var hasError = false

                val power = parseFloat(powerWatt)
                val factor = parseFloat(powerFactor)
                val hours = parseFloat(usageHours)

                if (selectedCircuit.isBlank()) {
                    circuitError = circuitErrorMsg
                    hasError = true
                }

                if (power <= 0f) {
                    powerError = powerErrorMsg
                    hasError = true
                }

                if (factor <= 0f) {
                    factorError = factorErrorMsg
                    hasError = true
                }

                if (quantity.toIntOrNull() == null || quantity.toInt() < 1) {
                    quantityError = quantityErrorMsg
                    hasError = true
                }

                if (hours !in 0f..24f) {
                    usageHoursError = usageHoursErrorMsg
                    hasError = true
                }

                if (selectedType == ApplianceType.OTHER &&
                    (otherName.isBlank() || otherDescription.isBlank())
                ) {
                    otherError = otherErrorMsg
                    hasError = true
                }

                if (!hasError) {
                    val circuitId = circuits.first { it.name == selectedCircuit }.circuitId

                    repeat(quantity.toInt()) { index ->
                        onSaveAppliance(
                            item.copy(
                                applianceId = generateId("A", selectedCircuit.take(1) + (index + 1)),
                                circuitId = circuitId,
                                powerWatt = power,
                                powerFactor = factor,
                                usageHoursPerDay = hours,
                                metadata = if (selectedType == ApplianceType.OTHER)
                                    mapOf("name" to otherName, "description" to otherDescription)
                                else item.metadata,
                                trace = buildTraceability(item.trace, ownerId ?: "")
                            )
                        )
                    }
                }
            }
        )
    }
}
