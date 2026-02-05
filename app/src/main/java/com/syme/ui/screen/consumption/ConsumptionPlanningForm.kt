package com.syme.ui.screen.consumption

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
fun ConsumptionPlanningForm(
    installationsId: List<String>,
    lastSubscriptions: Map<String, Long>,
    kWhPrice: Float = 49f,
    moneyUnit: String = "FCFA",
    energyUnit: String = "kWh",
    onSubmit: (installation: String, start: Long, end: Long, amount: Double) -> Unit
) {
    var selectedInstallation by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    var installationError by remember { mutableStateOf("") }
    var startDateError by remember { mutableStateOf("") }
    var endDateError by remember { mutableStateOf("") }

    val context = LocalContext.current
    val sdf = remember { TimeUtils.dateFormat }
    val today = remember { TimeUtils.currentTimestamp }

    // Erreurs
    val installationErrorMsg = stringResource(R.string.consumption_error_installation_required)
    val startDateErrorMsg = stringResource(R.string.consumption_error_start_date_invalid)
    val endDateErrorMsg = stringResource(R.string.consumption_error_end_date_invalid)

    fun parseDate(value: String): Long? =
        runCatching { sdf.parse(value)?.time }.getOrNull()

    val amountValue = amount.toDoubleOrNull() ?: 0.0
    val energyValue = if (kWhPrice > 0) amountValue / kWhPrice else 0.0

    // DatePickers Android
    val startCalendar = Calendar.getInstance()
    val endCalendar = Calendar.getInstance()

    val startDatePicker = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            startCalendar.set(year, month, dayOfMonth)
            startDate = sdf.format(startCalendar.time)
        },
        startCalendar.get(Calendar.YEAR),
        startCalendar.get(Calendar.MONTH),
        startCalendar.get(Calendar.DAY_OF_MONTH)
    )

    val endDatePicker = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            endCalendar.set(year, month, dayOfMonth)
            endDate = sdf.format(endCalendar.time)
        },
        endCalendar.get(Calendar.YEAR),
        endCalendar.get(Calendar.MONTH),
        endCalendar.get(Calendar.DAY_OF_MONTH)
    )

    fun validate(): Boolean {
        installationError = ""
        startDateError = ""
        endDateError = ""

        var valid = true

        val start = parseDate(startDate)
        val end = parseDate(endDate)

        if (selectedInstallation.isBlank()) {
            installationError = installationErrorMsg
            valid = false
        }

        if (start == null || start < today) {
            startDateError = startDateErrorMsg
            valid = false
        }

        val lastEnd = lastSubscriptions[selectedInstallation]
        if (start != null && lastEnd != null && start < lastEnd) {
            startDateError = startDateErrorMsg
            valid = false
        }

        if (end == null || (start != null && end < start)) {
            endDateError = endDateErrorMsg
            valid = false
        }

        if (amountValue <= 0) valid = false

        return valid
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            Text(
                text = stringResource(R.string.consumption_title),
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
                onValueChange = { selectedInstallation = it },
                label = stringResource(R.string.consumption_label_installation),
                error = installationError,
                items = installationsId
            )
        }

        item {
            DateField(
                value = startDate,
                onClick = { startDatePicker.show() },
                label = stringResource(R.string.consumption_label_start_date),
                error = startDateError
            )
        }

        item {
            DateField(
                value = endDate,
                onClick = { endDatePicker.show() },
                label = stringResource(R.string.consumption_label_end_date),
                error = endDateError
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
                            amountValue
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
fun ConsumptionPlanningFormPreview() {
    val installations = listOf("House A", "Shop B", "Factory C")
    val lastSubscriptions = mapOf(
        "House A" to Calendar.getInstance().apply { set(2026, 1, 5) }.timeInMillis,
        "Shop B" to Calendar.getInstance().apply { set(2026, 1, 20) }.timeInMillis
    )

    ConsumptionPlanningForm(
        installationsId = installations,
        lastSubscriptions = lastSubscriptions,
        kWhPrice = 49f,
        onSubmit = { _, _, _, _ -> }
    )
}
