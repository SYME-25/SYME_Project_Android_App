package com.syme.ui.component.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.R
import com.syme.domain.model.Consumption
import com.syme.domain.model.Measurement
import com.syme.domain.model.enumeration.ConsumptionStateType
import com.syme.ui.component.tank.TankLevelIndicator
import com.syme.ui.component.text.TextWithBackground
import com.syme.ui.theme.GreenTank
import com.syme.ui.theme.RedTank
import com.syme.ui.theme.YellowTank
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
    cardWidth: Dp = Dp.Unspecified,
    cardHeight: Dp = Dp.Unspecified,
    energyUnit: String = "kWh",
    powerUnit: String = "kW",
    realtimeMeasurements: List<Measurement> = emptyList(),
    onPauseToggle: (Boolean) -> Unit
) {
    val currentTime = System.currentTimeMillis()
    var isPaused by remember { mutableStateOf(consumption.consumptionState == ConsumptionStateType.PAUSED) }

    val dynamicState = when {
        consumption.consumptionState == ConsumptionStateType.ERROR -> ConsumptionStateType.ERROR
        isPaused -> ConsumptionStateType.PAUSED
        currentTime < consumption.periodStart -> ConsumptionStateType.WAITING
        currentTime in consumption.periodStart..consumption.periodEnd -> ConsumptionStateType.RUNNING
        else -> ConsumptionStateType.COMPLETED
    }

    // ── Tank level: energy-based for both Subscription and Demand ────────────
    val tankLevel: Float
    val tankLabel: String

    // Energy-based tank for both Subscription and Demand
    val realtimeKwh = if (dynamicState == ConsumptionStateType.RUNNING)
        realtimeMeasurements
            .filter { it.timestamp >= consumption.periodStart }
            .mapNotNull { it.energyActiveWh }
            .sum().div(1000.0)
    else 0.0

    val totalConsumed = if (dynamicState == ConsumptionStateType.RUNNING)
        (consumption.totalEnergy_kWhConsummed + realtimeKwh).coerceAtMost(consumption.totalEnergy_kWh.toDouble())
    else consumption.totalEnergy_kWhConsummed

    val remaining = (consumption.totalEnergy_kWh - totalConsumed).coerceAtLeast(0.0)
    tankLevel = if (consumption.totalEnergy_kWh > 0)
        (remaining / consumption.totalEnergy_kWh).coerceIn(0.0, 1.0).toFloat()
    else 0f
    tankLabel = "${round2(remaining)} $energyUnit"
    val tankColor: Color = when {
        tankLevel > 0.5f -> GreenTank
        tankLevel > 0.3f -> YellowTank
        else -> RedTank
    }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val periodStartText = dateFormat.format(Date(consumption.periodStart))
    val periodEndText   = dateFormat.format(Date(consumption.periodEnd))

    Card(
        modifier = modifier
            .sizeIn(minWidth = 360.dp, maxWidth = 360.dp, minHeight = 350.dp, maxHeight = 390.dp)
            .then(
                if (cardWidth != Dp.Unspecified && cardHeight != Dp.Unspecified)
                    Modifier.size(width = cardWidth, height = cardHeight)
                else Modifier.fillMaxWidth()
            )
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        border = BorderStroke(
            1.dp,
            if (consumption.onDemand)
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
            else
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = consumption.consumptionId, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = dynamicState.name,
                    color = when (dynamicState) {
                        ConsumptionStateType.RUNNING   -> MaterialTheme.colorScheme.primary
                        ConsumptionStateType.WAITING   -> MaterialTheme.colorScheme.onSurfaceVariant
                        ConsumptionStateType.COMPLETED -> MaterialTheme.colorScheme.secondary
                        ConsumptionStateType.PAUSED    -> MaterialTheme.colorScheme.tertiary
                        ConsumptionStateType.ERROR     -> MaterialTheme.colorScheme.error
                        else                           -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            // Body
            Box(modifier = Modifier.weight(4f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {

                    // Tank
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = tankLabel, fontWeight = FontWeight.Bold, color = tankColor, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        TankLevelIndicator(level = tankLevel, width = 80.dp, height = 200.dp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Info items
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Type chip
                        TypeChip(isOnDemand = consumption.onDemand)
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        InfoItem(
                            id = R.drawable.outline_home_24,
                            title = "Installation",
                            value = consumption.installationId ?: "-"
                        )
                        InfoItem(
                            id = R.drawable.outline_schedule_24,
                            title = "Periode",
                            value = "$periodStartText -> $periodEndText"
                        )

                        if (consumption.onDemand && consumption.requestedPowerKw != null) {
                            InfoItem(
                                id = R.drawable.outline_electric_bolt_24,
                                title = "Power",
                                value = "${consumption.requestedPowerKw.toInt()} $powerUnit"
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Countdown
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CountdownTimer(endTime = consumption.periodEnd, textColor = tankColor)
            }

            /*
            // Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                AppSwitch(
                    checked = isPaused,
                    onCheckedChange = { isPaused = it; onPauseToggle(it) },
                    label = "Pause"
                )
                AppTextButton(
                    text = "Delete",
                    onClick = {},
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_delete_24),
                            contentDescription = null
                        )
                    }
                )
            }*/
        }
    }
}

// ── TypeChip ──────────────────────────────────────────────────────────────────

@Composable
fun TypeChip(isOnDemand: Boolean) {
    val label = if (isOnDemand) "Demande" else "Subscription"
    val containerColor = if (isOnDemand)
        MaterialTheme.colorScheme.tertiaryContainer
    else
        MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (isOnDemand)
        MaterialTheme.colorScheme.onTertiaryContainer
    else
        MaterialTheme.colorScheme.onPrimaryContainer

    Surface(shape = RoundedCornerShape(50), color = containerColor) {
        Text(
            text = label,
            color = contentColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

// ── InfoItem ──────────────────────────────────────────────────────────────────

@Composable
fun InfoItem(id: Int, title: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = id),
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Text(text = value, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

// ── CountdownTimer ────────────────────────────────────────────────────────────

@Composable
fun CountdownTimer(endTime: Long, textColor: Color) {
    var remainingTime by remember { mutableLongStateOf(endTime - System.currentTimeMillis()) }
    LaunchedEffect(endTime) {
        while (remainingTime > 0) {
            remainingTime = endTime - System.currentTimeMillis()
            delay(1000)
        }
    }
    val duration = remainingTime.coerceAtLeast(0L).toDuration(DurationUnit.MILLISECONDS)
    val days    = duration.inWholeDays
    val hours   = duration.inWholeHours % 24
    val minutes = duration.inWholeMinutes % 60
    val seconds = duration.inWholeSeconds % 60
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        TextWithBackground(
            text = String.format("%dJ %02dh %02dm %02ds", days, hours, minutes, seconds),
            color = textColor
        )
    }
}

// ── ConsumptionRow ────────────────────────────────────────────────────────────

@Composable
fun ConsumptionRow(
    consumptions: List<Consumption>,
    onPauseToggle: (Consumption, Boolean) -> Unit,
    realtimeMeasurements: List<Measurement> = emptyList(),
    cardWidth: Dp = 360.dp,
    cardHeight: Dp = 370.dp,
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

    Box(modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp, max = 500.dp)) {
        if (limitedList.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = stringResource(R.string.no_subscriptions_found))
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(limitedList, key = { it.consumptionId }) { cons ->
                    ConsumptionCard(
                        consumption = cons,
                        cardWidth = cardWidth,
                        cardHeight = cardHeight,
                        realtimeMeasurements = if (cons.consumptionId == runningId) realtimeMeasurements else emptyList(),
                        onPauseToggle = { /*paused -> onPauseToggle(cons, paused)*/ }
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
