package com.syme.ui.screen.consumption

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

/**
 * Form to create a voluntary power reduction demand (onDemand = true).
 *
 * Rules:
 * - Start date >= end date of the last demand (onDemand=true) for this installation.
 *   If no previous demand exists, start date >= today.
 * - End date > start date (free choice).
 * - requestedPowerKw must be > 0 and < powerSubscribed.
 * - User can pick from suggested power levels (fixed kW values) OR type a custom value.
 *
 * @param installationsId        List of installation names for the dropdown.
 * @param lastDemandEnds         Map<installationName, lastDemandEndEpochMs> — only onDemand=true periods.
 * @param powerSubscribedByInstallation Map<installationName, powerSubscribed kW> — to enforce upper bound.
 * @param suggestedPowersKw      Fixed kW suggestions shown as chips (e.g. [100.0, 250.0, 500.0]).
 * @param powerUnit              Unit label displayed next to power values (default "kW").
 * @param onSubmit               Callback with (installationName, startMs, endMs, requestedPowerKw).
 */
@Composable
fun DemandForm(
    installationsId: List<String>,
    lastDemandEnds: Map<String, Long>,
    powerSubscribedByInstallation: Map<String, Double>,
    hasSubscriptionByInstallation: Map<String, Boolean>,
    suggestedPowersKw: List<Double> = emptyList(),
    kWPriceDemand: Float = 30f, // prix par kW en mode demande
    moneyUnit: String = "FCFA",
    powerUnit: String = "kW",
    onSubmit: (installation: String, start: Long, end: Long, requestedPowerKw: Double) -> Unit
) {
    var selectedInstallation by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var powerInput by remember { mutableStateOf("") }
    var selectedSuggestion by remember { mutableStateOf<Double?>(null) }

    var installationError by remember { mutableStateOf("") }
    var startDateError by remember { mutableStateOf("") }
    var endDateError by remember { mutableStateOf("") }
    var powerError by remember { mutableStateOf("") }

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

    // Minimum start = last demand end for selected installation, or today
    val minStartMs: Long by remember(selectedInstallation, lastDemandEnds) {
        derivedStateOf { lastDemandEnds[selectedInstallation] ?: today }
    }

    val minStartLabel by remember(minStartMs) {
        derivedStateOf { sdf.format(Date(minStartMs)) }
    }

    // Effective power = chip selection takes priority, otherwise the typed value
    val effectivePowerKw: Double by remember(selectedSuggestion, powerInput) {
        derivedStateOf { selectedSuggestion ?: powerInput.toDoubleOrNull() ?: 0.0 }
    }

    val powerSubscribed: Double by remember(selectedInstallation, powerSubscribedByInstallation) {
        derivedStateOf { powerSubscribedByInstallation[selectedInstallation] ?: Double.MAX_VALUE }
    }

    var subscriptionMessage by remember { mutableStateOf("") }
    val hasSubscription by remember(selectedInstallation, hasSubscriptionByInstallation) {
        derivedStateOf { hasSubscriptionByInstallation[selectedInstallation] ?: false }
    }

    fun parseDate(value: String): Long? =
        runCatching { sdf.parse(value)?.time }.getOrNull()

    // ── DatePickers ───────────────────────────────────────────────────────────
    val startCalendar = remember { Calendar.getInstance() }
    val endCalendar   = remember { Calendar.getInstance() }

    val startDatePicker = DatePickerDialog(
        context,
        { _, year, month, day ->
            startCalendar.set(year, month, day)
            startDate = sdf.format(startCalendar.time)
        },
        startCalendar.get(Calendar.YEAR),
        startCalendar.get(Calendar.MONTH),
        startCalendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        // Block dates before minStartMs
        datePicker.minDate = minStartMs
    }

    val endDatePicker = DatePickerDialog(
        context,
        { _, year, month, day ->
            endCalendar.set(year, month, day)
            endDate = sdf.format(endCalendar.time)
        },
        endCalendar.get(Calendar.YEAR),
        endCalendar.get(Calendar.MONTH),
        endCalendar.get(Calendar.DAY_OF_MONTH)
    )

    // ── Validation ────────────────────────────────────────────────────────────
    fun validate(): Boolean {
        installationError = ""
        startDateError = ""
        endDateError = ""
        powerError = ""
        var valid = true

        val start = parseDate(startDate)
        val end   = parseDate(endDate)

        if (selectedInstallation.isBlank()) {
            installationError = context.getString(R.string.consumption_error_installation_required)
            valid = false
        }
        if (start == null || start < minStartMs) {
            startDateError = context.getString(
                R.string.demand_error_start_date_invalid,
                minStartLabel
            )
            valid = false
        }
        if (end == null || (start != null && end <= start)) {
            endDateError = context.getString(R.string.consumption_error_end_date_invalid)
            valid = false
        }
        if (effectivePowerKw <= 0) {
            powerError = context.getString(R.string.demand_error_power_required)
            valid = false
        } else if (effectivePowerKw >= powerSubscribed) {
            powerError = context.getString(
                R.string.demand_error_power_exceeds_subscription,
                powerSubscribed,
                powerUnit
            )
            valid = false
        }
        return valid
    }

    val demandCost = effectivePowerKw * kWPriceDemand

    // ── UI ────────────────────────────────────────────────────────────────────
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
                text = stringResource(R.string.demand_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.fillMaxWidth().padding(start = 25.dp)
            )
        }

        item {
            Text(
                text = stringResource(R.string.demand_subtitle),
                fontSize = 18.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.fillMaxWidth().padding(start = 25.dp, top = 8.dp)
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Installation
        item {
            DropdownField(
                value = selectedInstallation,
                onValueChange = {
                    selectedInstallation = it
                    // Reset dates when installation changes
                    startDate = ""
                    endDate = ""
                },
                label = stringResource(R.string.consumption_label_installation),
                error = installationError,
                items = installationsId
            )
        }

        // Min start hint
        if (selectedInstallation.isNotBlank()) {
            item {
                Text(
                    text = stringResource(R.string.demand_start_date_hint, minStartLabel),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp, vertical = 2.dp)
                )
            }
        }

        // Start date — free picker, blocked before minStartMs
        item {
            DateField(
                value = startDate,
                onClick = {
                    // Refresh minDate each time the picker opens
                    startDatePicker.datePicker.minDate = minStartMs
                    startDatePicker.show()
                },
                label = stringResource(R.string.consumption_label_start_date),
                error = startDateError
            )
        }

        // End date — free picker, must be after start
        item {
            DateField(
                value = endDate,
                onClick = {
                    // Block end before start if start is already selected
                    parseDate(startDate)?.let { endDatePicker.datePicker.minDate = it + 86_400_000L }
                    endDatePicker.show()
                },
                label = stringResource(R.string.consumption_label_end_date),
                error = endDateError
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        // ── Power section ─────────────────────────────────────────────────────
        item {
            Text(
                text = stringResource(R.string.demand_label_power),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth().padding(start = 25.dp, bottom = 8.dp)
            )
        }

        // Suggestion chips
        if (suggestedPowersKw.isNotEmpty()) {
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(suggestedPowersKw) { suggestion ->
                        val isSelected = selectedSuggestion == suggestion
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedSuggestion = if (isSelected) null else suggestion
                                if (!isSelected) powerInput = "" // clear manual input
                            },
                            label = {
                                Text(
                                    text = "${suggestion.toInt()} $powerUnit",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.demand_label_or_custom),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 25.dp, top = 8.dp, bottom = 4.dp)
                )
            }
        }

        // Custom power input
        item {
            NumberField(
                value = powerInput,
                onValueChange = {
                    powerInput = it
                    selectedSuggestion = null // deselect chip when typing
                },
                label = stringResource(R.string.demand_label_power_input, powerUnit),
                error = powerError
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            Text(
                text = stringResource(
                    R.string.consumption_price_info,
                    powerUnit,
                    kWPriceDemand,
                    moneyUnit
                ),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 25.dp)
            )
        }
        // Affichage du prix de la puissance demandée
        if (effectivePowerKw > 0) {
            item { Spacer(modifier = Modifier.height(12.dp)) }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp)
                ){
                    TextWithBackground(
                        text = stringResource(
                            R.string.demand_price_info,
                            demandCost,
                            moneyUnit
                        ),
                        color = MaterialTheme.colorScheme.tertiary,
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        if (subscriptionMessage.isNotEmpty()) {
            item {
                Text(
                    text = subscriptionMessage,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 2,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp)
                )
            }
        }

        item {
            AppButton(
                text = stringResource(R.string.demand_button_submit),
                onClick = {
                    subscriptionMessage = ""

                    if (!hasSubscription) {
                        subscriptionMessage =
                            context.getString(R.string.demand_error_no_subscription)
                        return@AppButton
                    }

                    if (validate()) {
                        onSubmit(
                            selectedInstallation,
                            parseDate(startDate)!!,
                            parseDate(endDate)!!,
                            effectivePowerKw
                        )
                    }
                }
            )
        }

        item { Spacer(modifier = Modifier.height(44.dp)) }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun DemandFormPreview() {
    val installations = listOf("House A", "Shop B", "Factory C")
    DemandForm(
        installationsId = installations,
        hasSubscriptionByInstallation = mapOf(
            "House A" to true,
            "Shop B"  to true,
            "Factory C" to false),
        lastDemandEnds = mapOf(
            "House A" to Calendar.getInstance().apply { set(2026, 2, 10) }.timeInMillis,
            "Shop B"  to Calendar.getInstance().apply { set(2026, 3, 1) }.timeInMillis
            // Factory C has no previous demand → starts today
        ),
        powerSubscribedByInstallation = mapOf(
            "House A"   to 1000.0,
            "Shop B"    to 500.0,
            "Factory C" to 2000.0
        ),
        suggestedPowersKw = listOf(100.0, 250.0, 500.0, 750.0),
        onSubmit = { _, _, _, _ -> }
    )
}
