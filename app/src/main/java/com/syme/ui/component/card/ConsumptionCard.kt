package com.syme.ui.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Composable
fun ConsumptionCard(
    consumption: Consumption,
    modifier: Modifier = Modifier,
    cardWidth: Dp = 350.dp,
    energyUnit: String = "kWh",
    powerUnit: String = "kW",
    realtimeMeasurements: List<Measurement> = emptyList(),
    onPauseToggle: (Boolean) -> Unit = {}
) {
    val currentTime = System.currentTimeMillis()

    val dynamicState = when {
        consumption.consumptionState == ConsumptionStateType.ERROR    -> ConsumptionStateType.ERROR
        consumption.consumptionState == ConsumptionStateType.PAUSED   -> ConsumptionStateType.PAUSED
        currentTime < consumption.periodStart                         -> ConsumptionStateType.WAITING
        currentTime in consumption.periodStart..consumption.periodEnd -> ConsumptionStateType.RUNNING
        else                                                          -> ConsumptionStateType.COMPLETED
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

    val dateFormat = remember { SimpleDateFormat("dd MMM yy", Locale.getDefault()) }
    val periodStartText = remember(consumption.periodStart) {
        dateFormat.format(Date(consumption.periodStart))
    }
    val periodEndText = remember(consumption.periodEnd) {
        dateFormat.format(Date(consumption.periodEnd))
    }

    var showDetail by remember { mutableStateOf(false) }
    if (showDetail) {
        ConsumptionDetailDialog(
            consumption = consumption,
            energyUnit = energyUnit,
            powerUnit = powerUnit,
            realtimeMeasurements = realtimeMeasurements,
            onDismiss = { showDetail = false }
        )
    }

    Card(
        onClick = { showDetail = true },
        modifier = modifier
            .padding(horizontal = 6.dp, vertical = 5.dp)
            .width(cardWidth),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            if (consumption.onDemand)
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)
            else
                MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // ── BODY ──────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tank gauge replaces image
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TankLevelIndicator(
                        level = tankLevel,
                        width = 44.dp,
                        height = 100.dp
                    )
                    Text(
                        text = "${round2(remaining)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = tankColor,
                        fontSize = 10.sp
                    )
                    Text(
                        text = energyUnit,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        fontSize = 9.sp
                    )
                }

                // Info block
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // ID + chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // ← weight(1f) : l'ID prend le reste après les chips
                        Text(
                            text = consumption.consumptionId,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,   // ellipsis sur l'ID dans la card
                            letterSpacing = 0.3.sp,
                            modifier = Modifier.weight(1f)      // ← cède l'espace aux chips
                        )
                        // chips à droite, taille intrinsèque (pas de fillMaxWidth)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ConsumptionTypeChip(
                                isOnDemand = consumption.onDemand,
                                labelOnDemand = stringResource(R.string.consumption_type_on_demand),
                                labelSubscription = stringResource(R.string.consumption_type_subscription)
                            )
                            EntityBadge(
                                text = dynamicState.name,
                                color = stateColor
                                // ← supprimer Modifier.fillMaxWidth() qui forçait le badge à déborder
                            )
                        }
                    }

                    // Installation — main line
                    Text(
                        text = consumption.installationId ?: "-",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Period
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = periodStartText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "→",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                        )
                        Text(
                            text = periodEndText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }

                    // Power if on demand
                    if (consumption.onDemand && consumption.requestedPowerKw > 0.0) {
                        Text(
                            text = stringResource(
                                R.string.consumption_power_requested,
                                consumption.requestedPowerKw.toInt(),
                                powerUnit
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Progress bar — consumed vs total
                    if (consumption.totalEnergy_kWh > 0) {
                        val progress = (totalConsumed / consumption.totalEnergy_kWh)
                            .coerceIn(0.0, 1.0).toFloat()
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = tankColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${round2(totalConsumed)} $energyUnit",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    fontSize = 9.sp
                                )
                                Text(
                                    text = "${consumption.totalEnergy_kWh} $energyUnit",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }

            // ── FOOTER ────────────────────────────────────────────────────────
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(stateColor)
                    )
                    Text(
                        text = stringResource(R.string.consumption_label_remaining_time),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                // Separator
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .width(1.dp)
                        .height(12.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                )

                CountdownTimer(
                    endTime = consumption.periodEnd,
                    textColor = tankColor
                )
            }
        }
    }
}

// ── ConsumptionTypeChip ───────────────────────────────────────────────────────
@Composable
fun ConsumptionTypeChip(
    isOnDemand: Boolean,
    labelOnDemand: String,
    labelSubscription: String
) {
    val containerColor = if (isOnDemand)
        MaterialTheme.colorScheme.tertiaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    val contentColor = if (isOnDemand)
        MaterialTheme.colorScheme.onTertiaryContainer
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(containerColor)
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text = if (isOnDemand) labelOnDemand else labelSubscription,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.3.sp
        )
    }
}

// ── CountdownTimer ────────────────────────────────────────────────────────────
@Composable
fun CountdownTimer(
    endTime: Long,
    textColor: androidx.compose.ui.graphics.Color
) {
    var remainingTime by remember { mutableLongStateOf(endTime - System.currentTimeMillis()) }
    LaunchedEffect(endTime) {
        while (remainingTime > 0) {
            delay(1000)
            remainingTime = endTime - System.currentTimeMillis()
        }
    }
    val duration = remainingTime.coerceAtLeast(0L).toDuration(DurationUnit.MILLISECONDS)
    EntityBadge(
        text = String.format(
            "%dd %02dh %02dm %02ds",
            duration.inWholeDays,
            duration.inWholeHours % 24,
            duration.inWholeMinutes % 60,
            duration.inWholeSeconds % 60
        ),
        color = textColor
    )
}

// ── ConsumptionRow ────────────────────────────────────────────────────────────
@Composable
fun ConsumptionRow(
    consumptions: List<Consumption>,
    onPauseToggle: (Consumption, Boolean) -> Unit = { _, _ -> },
    realtimeMeasurements: List<Measurement> = emptyList(),
    cardWidth: Dp = 300.dp,
    maxItems: Int = 30
) {
    val now = System.currentTimeMillis()
    val limitedList = remember(consumptions) { consumptions.take(maxItems) }
    val runningId = remember(consumptions, now) {
        consumptions.firstOrNull {
            it.consumptionState != ConsumptionStateType.PAUSED &&
                    it.consumptionState != ConsumptionStateType.ERROR &&
                    now in it.periodStart..it.periodEnd
        }?.consumptionId
    }

    Box(modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp, max = 380.dp)) {
        if (limitedList.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(strokeWidth = 2.dp)
                Text(
                    text = stringResource(R.string.no_subscriptions_found),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(limitedList, key = { it.consumptionId }) { cons ->
                    ConsumptionCard(
                        consumption = cons,
                        cardWidth = cardWidth,
                        realtimeMeasurements = if (cons.consumptionId == runningId)
                            realtimeMeasurements else emptyList(),
                        onPauseToggle = { paused -> onPauseToggle(cons, paused) }
                    )
                }
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun ConsumptionCardStatesPreview() {
    val now = System.currentTimeMillis()
    val oneHour   = 3_600_000L
    val twoMonths = 5_184_000_000L

    val consumptions = listOf(
        Consumption(
            consumptionId = "C001", installationId = "H001",
            periodStart = now - oneHour, periodEnd = now + twoMonths,
            totalEnergy_kWh = 1000, totalEnergy_kWhConsummed = 320.0,
            consumptionState = ConsumptionStateType.RUNNING, onDemand = false
        ),
        Consumption(
            consumptionId = "C002", installationId = "H001",
            periodStart = now - oneHour, periodEnd = now + (oneHour * 48),
            totalEnergy_kWh = 0, totalEnergy_kWhConsummed = 0.0,
            consumptionState = ConsumptionStateType.RUNNING,
            onDemand = true, requestedPowerKw = 500.0
        ),
        Consumption(
            consumptionId = "C003", installationId = "H002",
            periodStart = now + oneHour, periodEnd = now + twoMonths,
            totalEnergy_kWh = 800, totalEnergy_kWhConsummed = 0.0,
            consumptionState = ConsumptionStateType.WAITING, onDemand = false
        ),
        Consumption(
            consumptionId = "C004", installationId = "H002",
            periodStart = now - oneHour, periodEnd = now + (oneHour * 24),
            totalEnergy_kWh = 0, totalEnergy_kWhConsummed = 0.0,
            consumptionState = ConsumptionStateType.PAUSED,
            onDemand = true, requestedPowerKw = 250.0
        )
    )

    LazyRow(
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(consumptions) { cons ->
            ConsumptionCard(consumption = cons, onPauseToggle = {})
        }
    }
}
