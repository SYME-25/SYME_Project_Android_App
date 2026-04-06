package com.syme.ui.screen.consumption

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.R
import com.syme.domain.model.enumeration.InputMode
import com.syme.ui.component.actionbutton.AppButton
import com.syme.ui.component.field.DateField
import com.syme.ui.component.field.DropdownField
import com.syme.ui.component.field.NumberField
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
    onSubmit: (installation: String, start: Long, end: Long, energyWh: Double, powerKw: Double) -> Unit
    //                                                                          ↑ nouveau paramètre
) {
    val colorPrimary = MaterialTheme.colorScheme.primary
    val colorAccent = MaterialTheme.colorScheme.secondary
    val colorLightBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val colorAccentBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    val colorGray = MaterialTheme.colorScheme.secondary

    var selectedInstallation by remember { mutableStateOf("") }
    var startDate            by remember { mutableStateOf("") }
    var endDate              by remember { mutableStateOf("") }
    var amount               by remember { mutableStateOf("") }
    var energy               by remember { mutableStateOf("") }
    var requestedPowerKw     by remember { mutableStateOf("") }
    var inputMode            by remember { mutableStateOf(InputMode.AMOUNT) }

    var installationError by remember { mutableStateOf("") }
    var startDateError    by remember { mutableStateOf("") }
    var powerError        by remember { mutableStateOf("") }

    val context = LocalContext.current
    val sdf     = remember { TimeUtils.dateFormat }
    val today   = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun parseDate(v: String): Long? = runCatching { sdf.parse(v)?.time }.getOrNull()

    val minStartMs by remember(selectedInstallation, lastSubscriptions) {
        derivedStateOf { lastSubscriptions[selectedInstallation] ?: today }
    }
    val minStartLabel by remember(minStartMs) {
        derivedStateOf { sdf.format(Date(minStartMs)) }
    }

    // ── Calculs croisés ───────────────────────────────────────────────────────
    val amountValue  = amount.toDoubleOrNull() ?: 0.0
    val energyValue  = energy.toDoubleOrNull() ?: 0.0
    val derivedEnergy = if (inputMode == InputMode.AMOUNT && kWhPrice > 0) amountValue / kWhPrice else energyValue
    val derivedAmount = if (inputMode == InputMode.ENERGY && kWhPrice > 0) energyValue * kWhPrice else amountValue
    val powerValue   = requestedPowerKw.toDoubleOrNull() ?: 0.0

    // DatePicker
    val startCalendar = remember { Calendar.getInstance() }
    val startDatePicker = DatePickerDialog(
        context,
        { _, year, month, day ->
            startCalendar.set(year, month, day)
            val endCal = (startCalendar.clone() as Calendar).apply { add(Calendar.MONTH, 2) }
            startDate = sdf.format(startCalendar.time)
            endDate   = sdf.format(endCal.time)
        },
        startCalendar.get(Calendar.YEAR),
        startCalendar.get(Calendar.MONTH),
        startCalendar.get(Calendar.DAY_OF_MONTH)
    ).apply { datePicker.minDate = minStartMs }

    fun validate(): Boolean {
        installationError = ""; startDateError = ""; powerError = ""
        var valid = true
        val start = parseDate(startDate)
        if (selectedInstallation.isBlank()) {
            installationError = context.getString(R.string.consumption_error_installation_required)
            valid = false
        }
        if (start == null || start < minStartMs) {
            startDateError = context.getString(R.string.consumption_error_start_date_overlap, minStartLabel)
            valid = false
        }
        if (powerValue <= 0) {
            powerError = context.getString(R.string.consumption_error_installation_required) // réutilise ou crée une string dédiée
            valid = false
        }
        if (derivedEnergy <= 0) valid = false
        if (parseDate(endDate) == null) valid = false
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
                    .background(Brush.horizontalGradient(listOf(colorPrimary, colorAccent)))
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.subscription_title),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.consumption_subtitle),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }

        item { Spacer(Modifier.height(20.dp)) }

        // ── INSTALLATION ──────────────────────────────────────────────────────
        item {
            SectionLabel("Installation", Icons.Default.HomeWork, colorPrimary)
        }
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
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = colorPrimary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.consumption_error_start_date_overlap, minStartLabel),
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
                        onClick = { startDatePicker.show() },
                        label = stringResource(R.string.consumption_label_start_date),
                        error = startDateError
                    )
                }
                Box(Modifier.weight(1f)) {
                    DateField(
                        value = endDate,
                        onClick = {},
                        label = stringResource(R.string.consumption_label_end_date),
                        error = ""
                    )
                }
            }
        }

        item { Spacer(Modifier.height(12.dp)) }

        // ── PUISSANCE ─────────────────────────────────────────────────────────
        item { SectionLabel("Requested Power", Icons.Default.ElectricBolt, colorPrimary) }
        item {
            NumberField(
                value = requestedPowerKw,
                onValueChange = { requestedPowerKw = it },
                label = "Power (kW)",
                error = powerError
            )
        }
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

        // ── MODE TOGGLE ───────────────────────────────────────────────────────
        item { SectionLabel("Budget", Icons.Default.Calculate, colorPrimary) }
        item {
            InputModeToggle(
                selected = inputMode,
                onSelect = {
                    inputMode = it
                    amount = ""; energy = ""
                },
                moneyUnit = moneyUnit,
                energyUnit = energyUnit,
                colorPrimary = colorPrimary,
                colorAccentBg = colorAccentBg
            )
        }
        item { Spacer(Modifier.height(8.dp)) }

        // ── CHAMP ACTIF ───────────────────────────────────────────────────────
        item {
            AnimatedContent(
                targetState = inputMode,
                transitionSpec = {
                    fadeIn(tween(250)) togetherWith fadeOut(tween(200))
                },
                label = "inputModeContent"
            ) { mode ->
                if (mode == InputMode.AMOUNT) {
                    NumberField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = stringResource(R.string.consumption_label_amount, moneyUnit),
                        error = ""
                    )
                } else {
                    NumberField(
                        value = energy,
                        onValueChange = { energy = it },
                        label = "Energy ($energyUnit)",
                        error = ""
                    )
                }
            }
        }

        // ── RÉSULTAT CALCULÉ ──────────────────────────────────────────────────
        item {
            AnimatedVisibility(
                visible = derivedEnergy > 0 || derivedAmount > 0,
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
                    // Énergie
                    ResultChip(
                        icon = Icons.Default.Bolt,
                        label = "Energy",
                        value = "${derivedEnergy.roundToInt()} $energyUnit"
                    )
                    // Diviseur
                    Box(Modifier.width(1.dp).height(36.dp).background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)))
                    // Montant
                    ResultChip(
                        icon = Icons.Default.Payments,
                        label = "Amount",
                        value = "${derivedAmount.roundToInt()} $moneyUnit"
                    )
                    // Tarif
                    Box(Modifier.width(1.dp).height(36.dp).background(Color.White.copy(alpha = 0.3f)))
                    ResultChip(
                        icon = Icons.Default.Tag,
                        label = "Rate",
                        value = "$kWhPrice $moneyUnit/kWh"
                    )
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }

        // ── SUBMIT ────────────────────────────────────────────────────────────
        item {
            AppButton(
                text = stringResource(R.string.consumption_button_submit),
                onClick = {
                    if (validate()) {
                        onSubmit(
                            selectedInstallation,
                            parseDate(startDate)!!,
                            parseDate(endDate)!!,
                            derivedEnergy,
                            powerValue
                        )
                    }
                }
            )
        }

        item { Spacer(Modifier.height(44.dp)) }
    }
}

// ── Sub-components ────────────────────────────────────────────────────────────

@Composable
fun SectionLabel(text: String, icon: ImageVector, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 25.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        }
        Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = color, letterSpacing = 0.4.sp)
    }
}

@Composable
private fun InputModeToggle(
    selected: InputMode,
    onSelect: (InputMode) -> Unit,
    moneyUnit: String,
    energyUnit: String,
    colorPrimary: Color,
    colorAccentBg: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 25.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colorAccentBg)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf(
            InputMode.AMOUNT to "Enter $moneyUnit",
            InputMode.ENERGY to "Enter kWh"
        ).forEach { (mode, label) ->
            val isSelected = selected == mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(
                        if (isSelected) colorPrimary
                        else MaterialTheme.colorScheme.surface
                    )
                    .clickable { onSelect(mode) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
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

@Composable
fun ResultChip(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
        Spacer(Modifier.height(2.dp))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimary)
        Text(text = label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f))
    }
}

@Preview(showBackground = true)
@Composable
fun SubscriptionFormPreview() {
    SubscriptionForm(
        installationsId = listOf("House A", "Shop B", "Factory C"),
        lastSubscriptions = mapOf("House A" to Calendar.getInstance().apply { set(2026, 1, 5) }.timeInMillis),
        onSubmit = { _, _, _, _, _ -> }
    )
}