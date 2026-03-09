package com.syme.ui.screen.consumption

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.R
import com.syme.ui.component.actionbutton.AppButton
import com.syme.ui.component.field.DateField
import com.syme.ui.component.field.DropdownField
import com.syme.ui.component.field.NumberField
import com.syme.ui.component.text.TextWithBackground
import com.syme.utils.TimeUtils
import java.util.*
import kotlin.math.roundToInt

@Composable
fun SubscriptionForm(
    installationsId: List<String>,
    lastSubscriptions: Map<String, Long>,
    kWhPrice: Float = 49f,
    moneyUnit: String = "FCFA",
    energyUnit: String = "kWh",
    onSubmit: (installation: String, start: Long, end: Long, energyWh: Double) -> Unit
) {
    var selectedInstallation by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    var installationError by remember { mutableStateOf("") }
    var startDateError by remember { mutableStateOf("") }

    val context = LocalContext.current
    val sdf = remember { TimeUtils.dateFormat }

    val today = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun parseDate(value: String): Long? =
        runCatching { sdf.parse(value)?.time }.getOrNull()

    // 🔹 date minimale autorisée pour start
    val minStartMs by remember(selectedInstallation, lastSubscriptions) {
        derivedStateOf { lastSubscriptions[selectedInstallation] ?: today }
    }

    val minStartLabel by remember(minStartMs) {
        derivedStateOf { sdf.format(Date(minStartMs)) }
    }

    val amountValue = amount.toDoubleOrNull() ?: 0.0
    val energyValue = if (kWhPrice > 0) amountValue / kWhPrice else 0.0

    // DatePickers
    val startCalendar = remember { Calendar.getInstance() }

    val startDatePicker = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            startCalendar.set(year, month, dayOfMonth)

            val startMillis = startCalendar.timeInMillis

            // ⚡ fin = +2 mois automatique
            val endCalendar = startCalendar.clone() as Calendar
            endCalendar.add(Calendar.MONTH, 2)

            startDate = sdf.format(startCalendar.time)
            endDate = sdf.format(endCalendar.time)
        },
        startCalendar.get(Calendar.YEAR),
        startCalendar.get(Calendar.MONTH),
        startCalendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = minStartMs
    }

    fun validate(): Boolean {
        installationError = ""
        startDateError = ""

        var valid = true

        val start = parseDate(startDate)
        val end = parseDate(endDate)

        if (selectedInstallation.isBlank()) {
            installationError =
                context.getString(R.string.consumption_error_installation_required)
            valid = false
        }

        if (start == null || start < minStartMs) {
            startDateError = context.getString(
                R.string.consumption_error_start_date_overlap,
                minStartLabel
            )
            valid = false
        }

        if (amountValue <= 0) valid = false
        if (start == null || end == null) valid = false

        return valid
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            Text(
                text = stringResource(R.string.subscription_title),
                fontSize = 24.sp,
                modifier = Modifier.fillMaxWidth().padding(start = 25.dp),
                fontWeight = FontWeight.ExtraBold
            )
        }

        item {
            Text(
                text = stringResource(R.string.consumption_subtitle),
                fontSize = 18.sp,
                modifier = Modifier.fillMaxWidth().padding(start = 25.dp, top = 8.dp),
                fontWeight = FontWeight.Light
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            DropdownField(
                value = selectedInstallation,
                onValueChange = {
                    selectedInstallation = it
                    startDate = ""
                    endDate = ""
                },
                label = stringResource(R.string.consumption_label_installation),
                error = installationError,
                items = installationsId
            )
        }

        if (selectedInstallation.isNotBlank()) {
            item {
                Text(
                    text = stringResource(
                        R.string.consumption_error_start_date_overlap,
                        minStartLabel
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp, vertical = 4.dp)
                )
            }
        }

        // Start date (editable)
        item {
            DateField(
                value = startDate,
                onClick = { startDatePicker.show() },
                label = stringResource(R.string.consumption_label_start_date),
                error = startDateError
            )
        }

        // End date (auto, non cliquable)
        item {
            DateField(
                value = endDate,
                onClick = {},
                label = stringResource(R.string.consumption_label_end_date),
                error = ""
            )
        }

        item {
            NumberField(
                value = amount,
                onValueChange = { amount = it },
                label = stringResource(R.string.consumption_label_amount, moneyUnit),
                error = ""
            )
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
            Text(
                text = stringResource(
                    R.string.consumption_price_info,
                    energyUnit,
                    kWhPrice,
                    moneyUnit
                ),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 25.dp)
            )
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                TextWithBackground(
                    text = stringResource(
                        R.string.consumption_energy_result,
                        energyValue.roundToInt(),
                        energyUnit
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textStyle = MaterialTheme.typography.bodyLarge
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            AppButton(
                text = stringResource(R.string.consumption_button_submit),
                onClick = {
                    if (validate()) {
                        onSubmit(
                            selectedInstallation,
                            parseDate(startDate)!!,
                            parseDate(endDate)!!,
                            energyValue
                        )
                    }
                }
            )
        }

        item { Spacer(modifier = Modifier.height(44.dp)) }
    }
}

@Preview(showBackground = true)
@Composable
fun SubscriptionFormPreview() {
    val installations = listOf("House A", "Shop B", "Factory C")
    val lastSubscriptions = mapOf(
        "House A" to Calendar.getInstance().apply { set(2026, 1, 5) }.timeInMillis,
        "Shop B" to Calendar.getInstance().apply { set(2026, 1, 20) }.timeInMillis
    )

    SubscriptionForm(
        installationsId = installations,
        lastSubscriptions = lastSubscriptions,
        kWhPrice = 49f,
        onSubmit = { _, _, _, _ -> }
    )
}
