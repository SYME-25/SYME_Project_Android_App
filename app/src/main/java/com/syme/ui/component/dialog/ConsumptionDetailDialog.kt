package com.syme.ui.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.syme.R
import com.syme.domain.model.Consumption
import com.syme.domain.model.Measurement
import com.syme.domain.model.enumeration.ConsumptionStateType
import com.syme.ui.component.tank.TankLevelIndicator
import com.syme.ui.component.text.EntityBadge
import com.syme.ui.theme.TankGreen
import com.syme.ui.theme.TankRed
import com.syme.ui.theme.TankYellow
import com.syme.utils.round2
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ConsumptionDetailDialog(
    consumption: Consumption,
    energyUnit: String = "kWh",
    powerUnit: String = "kW",
    realtimeMeasurements: List<Measurement> = emptyList(),
    onDismiss: () -> Unit
) {
    // ── Calculs identiques à ConsumptionCard ─────────────────────────────
    val currentTime = System.currentTimeMillis()
    val dynamicState = when {
        consumption.consumptionState == ConsumptionStateType.ERROR   -> ConsumptionStateType.ERROR
        consumption.consumptionState == ConsumptionStateType.PAUSED  -> ConsumptionStateType.PAUSED
        currentTime < consumption.periodStart                        -> ConsumptionStateType.WAITING
        currentTime in consumption.periodStart..consumption.periodEnd -> ConsumptionStateType.RUNNING
        else                                                         -> ConsumptionStateType.COMPLETED
    }
    val realtimeKwh = if (dynamicState == ConsumptionStateType.RUNNING)
        realtimeMeasurements
            .filter { it.timestamp >= consumption.periodStart }
            .mapNotNull { it.energyActiveWh }
            .sum() / 1000.0
    else 0.0

    val totalConsumed = (consumption.totalEnergy_kWhConsummed + realtimeKwh)
        .coerceAtMost(consumption.totalEnergy_kWh.toDouble())
    val remaining = (consumption.totalEnergy_kWh - totalConsumed).coerceAtLeast(0.0)
    val tankLevel = if (consumption.totalEnergy_kWh > 0)
        (remaining / consumption.totalEnergy_kWh).coerceIn(0.0, 1.0).toFloat()
    else 0f

    val tankColor = when {
        tankLevel > 0.5f -> TankGreen
        tankLevel > 0.3f -> TankYellow
        else             -> TankRed
    }
    val stateColor = when (dynamicState) {
        ConsumptionStateType.RUNNING   -> MaterialTheme.colorScheme.primary
        ConsumptionStateType.WAITING   -> MaterialTheme.colorScheme.onSurfaceVariant
        ConsumptionStateType.COMPLETED -> MaterialTheme.colorScheme.secondary
        ConsumptionStateType.PAUSED    -> MaterialTheme.colorScheme.tertiary
        ConsumptionStateType.ERROR     -> MaterialTheme.colorScheme.error
        else                           -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy · HH:mm", Locale.getDefault()) }
    val periodStartText = remember(consumption.periodStart) {
        dateFormat.format(Date(consumption.periodStart))
    }
    val periodEndText = remember(consumption.periodEnd) {
        dateFormat.format(Date(consumption.periodEnd))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                // ── Header coloré ─────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = stateColor.copy(alpha = 0.10f),
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Tank
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TankLevelIndicator(
                                level = tankLevel,
                                width = 40.dp,
                                height = 80.dp
                            )
                            Text(
                                text = "${round2(remaining)} $energyUnit",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = tankColor,
                                fontSize = 9.sp
                            )
                        }
                        // Titre + état
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                EntityBadge(text = dynamicState.name, color = stateColor)
                                ConsumptionTypeChip(
                                    isOnDemand = consumption.onDemand,
                                    labelOnDemand = stringResource(R.string.consumption_type_on_demand),
                                    labelSubscription = stringResource(R.string.consumption_type_subscription)
                                )
                            }
                            Text(
                                text = consumption.installationId ?: "-",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            // ID complet — affiché sans troncature ici
                            Text(
                                text = consumption.consumptionId,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                    // Bouton fermeture
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = "Fermer",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // ── Section Énergie ───────────────────────────────────
                    if (consumption.totalEnergy_kWh > 0) {
                        DetailSectionTitle(
                            icon = Icons.Outlined.BatteryChargingFull,
                            title = stringResource(R.string.consumption_detail_energy_section)
                            // "Énergie"
                        )
                        val progress = (totalConsumed / consumption.totalEnergy_kWh)
                            .coerceIn(0.0, 1.0).toFloat()

                        // Barre de progression grande
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = tankColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.consumption_detail_consumed,
                                        round2(totalConsumed) ?: 0.0,
                                        energyUnit
                                    ), // "Consommé : X kWh"
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = stringResource(
                                        R.string.consumption_detail_total,
                                        consumption.totalEnergy_kWh, energyUnit
                                    ), // "Total : X kWh"
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DetailMetricCard(
                                label = stringResource(R.string.consumption_detail_remaining),
                                // "Restant"
                                value = "${round2(remaining)} $energyUnit",
                                valueColor = tankColor,
                                modifier = Modifier.weight(1f)
                            )
                            DetailMetricCard(
                                label = stringResource(R.string.consumption_detail_realtime),
                                // "Temps réel"
                                value = if (realtimeKwh > 0) "${round2(realtimeKwh)} $energyUnit"
                                else "-",
                                valueColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // ── Section Période ───────────────────────────────────
                    DetailSectionTitle(
                        icon = Icons.Outlined.CalendarMonth,
                        title = stringResource(R.string.consumption_detail_period_section)
                        // "Période"
                    )
                    DetailRow(
                        icon = Icons.Outlined.PlayArrow,
                        label = stringResource(R.string.consumption_detail_start),
                        // "Début"
                        value = periodStartText
                    )
                    DetailRow(
                        icon = Icons.Outlined.Stop,
                        label = stringResource(R.string.consumption_detail_end),
                        // "Fin"
                        value = periodEndText
                    )
                    DetailRow(
                        icon = Icons.Outlined.Timer,
                        label = stringResource(R.string.consumption_detail_duration),
                        // "Durée"
                        value = formatDuration(consumption.durationMs)
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // ── Section Puissance / Demande ───────────────────────
                    if (consumption.onDemand || consumption.requestedPowerKw > 0.0) {
                        DetailSectionTitle(
                            icon = Icons.Outlined.ElectricBolt,
                            title = stringResource(R.string.consumption_detail_power_section)
                            // "Puissance"
                        )
                        if (consumption.requestedPowerKw > 0.0) {
                            DetailRow(
                                icon = Icons.Outlined.Tune,
                                label = stringResource(R.string.consumption_detail_requested_power),
                                // "Puissance demandée"
                                value = "${consumption.requestedPowerKw.toInt()} $powerUnit"
                            )
                        }
                        if (consumption.onDemand) {
                            DetailRow(
                                icon = Icons.Outlined.Info,
                                label = stringResource(R.string.consumption_detail_type),
                                // "Type"
                                value = stringResource(R.string.consumption_type_on_demand)
                            )
                        }
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // ── Section IDs techniques ────────────────────────────
                    DetailSectionTitle(
                        icon = Icons.Outlined.Tag,
                        title = stringResource(R.string.consumption_detail_ids_section)
                        // "Identifiants"
                    )
                    DetailRow(
                        icon = Icons.Outlined.Bolt,
                        label = stringResource(R.string.consumption_detail_consumption_id),
                        // "ID Consommation"
                        value = consumption.consumptionId,
                        valueMonospace = true
                    )
                    consumption.installationId?.let {
                        DetailRow(
                            icon = Icons.Outlined.Home,
                            label = stringResource(R.string.consumption_detail_installation_id),
                            // "Installation"
                            value = it,
                            valueMonospace = true
                        )
                    }
                    consumption.meterId?.let {
                        DetailRow(
                            icon = Icons.Outlined.Speed,
                            label = stringResource(R.string.consumption_detail_meter_id),
                            // "Compteur"
                            value = it,
                            valueMonospace = true
                        )
                    }

                    // ── Countdown ─────────────────────────────────────────
                    if (dynamicState == ConsumptionStateType.RUNNING ||
                        dynamicState == ConsumptionStateType.WAITING
                    ) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(stateColor)
                                )
                                Text(
                                    text = stringResource(R.string.consumption_label_remaining_time),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                            CountdownTimer(
                                endTime = consumption.periodEnd,
                                textColor = tankColor
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Sous-composants internes ──────────────────────────────────────────────────

@Composable
private fun DetailSectionTitle(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueMonospace: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
            modifier = Modifier
                .size(15.dp)
                .padding(top = 2.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.width(110.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = if (valueMonospace) FontFamily.Monospace else null,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DetailMetricCard(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

private fun formatDuration(ms: Long): String {
    if (ms <= 0L) return "-"
    val days    = ms / 86_400_000L
    val hours   = (ms % 86_400_000L) / 3_600_000L
    val minutes = (ms % 3_600_000L) / 60_000L
    return when {
        days > 0  -> "${days}j ${hours}h"
        hours > 0 -> "${hours}h ${minutes}min"
        else      -> "${minutes}min"
    }
}