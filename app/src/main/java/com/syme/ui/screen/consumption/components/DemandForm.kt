package com.syme.ui.screen.consumption

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.syme.utils.TimeUtils
import java.util.*

@Composable
fun DemandForm(
    installationsId: List<String>,
    lastDemandEnds: Map<String, Long>,
    powerSubscribedByInstallation: Map<String, Double>,
    hasSubscriptionByInstallation: Map<String, Boolean>,
    suggestedPowersKw: List<Double> = emptyList(),
    kWPriceDemand: Float = 30f,
    moneyUnit: String = "FCFA",
    powerUnit: String = "kW",
    onSubmit: (installation: String, start: Long, end: Long, requestedPowerKw: Double) -> Unit
) {
    val colorPrimary = MaterialTheme.colorScheme.primary
    val colorAccent = MaterialTheme.colorScheme.secondary
    val colorAccentBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)

    var selectedInstallation  by remember { mutableStateOf("") }
    var startDate             by remember { mutableStateOf("") }
    var endDate               by remember { mutableStateOf("") }
    var powerInput            by remember { mutableStateOf("") }
    var selectedSuggestion    by remember { mutableStateOf<Double?>(null) }
    var installationError     by remember { mutableStateOf("") }
    var startDateError        by remember { mutableStateOf("") }
    var endDateError          by remember { mutableStateOf("") }
    var powerError            by remember { mutableStateOf("") }
    var subscriptionMessage   by remember { mutableStateOf("") }

    val context = LocalContext.current
    val sdf     = remember { TimeUtils.dateFormat }
    val today   = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val minStartMs by remember(selectedInstallation, lastDemandEnds) {
        derivedStateOf { lastDemandEnds[selectedInstallation] ?: today }
    }
    val minStartLabel by remember(minStartMs) {
        derivedStateOf { sdf.format(Date(minStartMs)) }
    }
    val effectivePowerKw by remember(selectedSuggestion, powerInput) {
        derivedStateOf { selectedSuggestion ?: powerInput.toDoubleOrNull() ?: 0.0 }
    }
    val powerSubscribed by remember(selectedInstallation, powerSubscribedByInstallation) {
        derivedStateOf { powerSubscribedByInstallation[selectedInstallation] ?: Double.MAX_VALUE }
    }
    val hasSubscription by remember(selectedInstallation, hasSubscriptionByInstallation) {
        derivedStateOf { hasSubscriptionByInstallation[selectedInstallation] ?: false }
    }
    val demandCost = effectivePowerKw * kWPriceDemand

    fun parseDate(v: String): Long? = runCatching { sdf.parse(v)?.time }.getOrNull()

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
    ).apply { datePicker.minDate = minStartMs }

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

    fun validate(): Boolean {
        installationError = ""; startDateError = ""; endDateError = ""; powerError = ""
        var valid = true
        val start = parseDate(startDate)
        val end   = parseDate(endDate)
        if (selectedInstallation.isBlank()) {
            installationError = context.getString(R.string.consumption_error_installation_required)
            valid = false
        }
        if (start == null || start < minStartMs) {
            startDateError = context.getString(R.string.demand_error_start_date_invalid, minStartLabel)
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
            powerError = context.getString(R.string.demand_error_power_exceeds_subscription, powerSubscribed, powerUnit)
            valid = false
        }
        return valid
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { Spacer(Modifier.height(24.dp)) }

        // ── HEADER GRADIENT ───────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.primary
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.demand_title),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.demand_subtitle),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }

        item { Spacer(Modifier.height(20.dp)) }

        // ── INSTALLATION ──────────────────────────────────────────────────────
        item { SectionLabel("Installation", Icons.Default.HomeWork, colorPrimary) }
        item {
            DropdownField(
                value = selectedInstallation,
                onValueChange = { selectedInstallation = it; startDate = ""; endDate = "" },
                label = stringResource(R.string.consumption_label_installation),
                error = installationError,
                items = installationsId
            )
        }
        if (selectedInstallation.isNotBlank()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colorAccentBg)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = colorPrimary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.demand_start_date_hint, minStartLabel),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorPrimary
                    )
                }
            }
        }

        item { Spacer(Modifier.height(12.dp)) }

        // ── DATES ─────────────────────────────────────────────────────────────
        item { SectionLabel("Period", Icons.Default.DateRange, colorPrimary) }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(Modifier.weight(1f)) {
                    DateField(
                        value = startDate,
                        onClick = {
                            startDatePicker.datePicker.minDate = minStartMs
                            startDatePicker.show()
                        },
                        label = stringResource(R.string.consumption_label_start_date),
                        error = startDateError
                    )
                }
                Box(Modifier.weight(1f)) {
                    DateField(
                        value = endDate,
                        onClick = {
                            parseDate(startDate)?.let { endDatePicker.datePicker.minDate = it + 86_400_000L }
                            endDatePicker.show()
                        },
                        label = stringResource(R.string.consumption_label_end_date),
                        error = endDateError
                    )
                }
            }
        }

        item { Spacer(Modifier.height(12.dp)) }

        // ── POWER SECTION ─────────────────────────────────────────────────────
        item { SectionLabel(stringResource(R.string.demand_label_power), Icons.Default.ElectricBolt, colorPrimary) }

        // Chips de suggestion
        if (suggestedPowersKw.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colorAccentBg)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    suggestedPowersKw.forEach { suggestion ->
                        val isSelected = selectedSuggestion == suggestion
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .background(
                                    if (isSelected) colorPrimary
                                    else MaterialTheme.colorScheme.surface
                                )
                                .clickable {
                                    selectedSuggestion = if (isSelected) null else suggestion
                                    if (!isSelected) powerInput = ""
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${suggestion.toInt()} $powerUnit",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                }
                            )
                        }
                    }
                }
            }
            item {
                Text(
                    text = stringResource(R.string.demand_label_or_custom),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp, vertical = 6.dp)
                )
            }
        }

        // Champ custom
        item {
            NumberField(
                value = powerInput,
                onValueChange = { powerInput = it; selectedSuggestion = null },
                label = stringResource(R.string.demand_label_power_input, powerUnit),
                error = powerError
            )
        }

        // Warning power < subscribed
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.consumption_warning_power_reduction),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        item { Spacer(Modifier.height(16.dp)) }

        // ── RÉSULTAT COÛT ─────────────────────────────────────────────────────
        item {
            AnimatedVisibility(
                visible = effectivePowerKw > 0,
                enter = fadeIn(tween(300)) + expandVertically(),
                exit  = fadeOut(tween(200)) + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.secondary,
                                    MaterialTheme.colorScheme.primary
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ResultChip(
                        icon = Icons.Default.ElectricBolt,
                        label = "Power",
                        value = "${effectivePowerKw.toInt()} $powerUnit"
                    )
                    Box(Modifier.width(1.dp).height(36.dp).background(Color.White.copy(alpha = 0.3f)))
                    ResultChip(
                        icon = Icons.Default.Payments,
                        label = "Est. Cost",
                        value = "${demandCost.toInt()} $moneyUnit"
                    )
                    Box(Modifier.width(1.dp).height(36.dp).background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)))
                    ResultChip(
                        icon = Icons.Default.Tag,
                        label = "Rate",
                        value = "$kWPriceDemand $moneyUnit/$powerUnit"
                    )
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }

        // ── ERREUR SUBSCRIPTION ───────────────────────────────────────────────
        if (subscriptionMessage.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = subscriptionMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }

        // ── SUBMIT ────────────────────────────────────────────────────────────
        item {
            AppButton(
                text = stringResource(R.string.demand_button_submit),
                onClick = {
                    subscriptionMessage = ""
                    if (!hasSubscription) {
                        subscriptionMessage = context.getString(R.string.demand_error_no_subscription)
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

        item { Spacer(Modifier.height(44.dp)) }
    }
}

@Preview(showBackground = true)
@Composable
fun DemandFormPreview() {
    DemandForm(
        installationsId = listOf("House A", "Shop B", "Factory C"),
        hasSubscriptionByInstallation = mapOf("House A" to true, "Shop B" to true, "Factory C" to false),
        lastDemandEnds = mapOf(
            "House A" to Calendar.getInstance().apply { set(2026, 2, 10) }.timeInMillis,
            "Shop B"  to Calendar.getInstance().apply { set(2026, 3, 1) }.timeInMillis
        ),
        powerSubscribedByInstallation = mapOf("House A" to 1000.0, "Shop B" to 500.0, "Factory C" to 2000.0),
        suggestedPowersKw = listOf(100.0, 250.0, 500.0, 750.0),
        onSubmit = { _, _, _, _ -> }
    )
}