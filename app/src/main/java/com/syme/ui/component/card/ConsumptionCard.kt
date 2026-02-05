package com.syme.ui.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.syme.domain.model.Consumption
import com.syme.domain.model.enumeration.ConsumptionStateType
import com.syme.ui.component.actionbutton.AppSwitch
import com.syme.ui.component.tank.TankLevelIndicator
import com.syme.R
import com.syme.ui.component.actionbutton.AppButton
import com.syme.ui.component.actionbutton.AppTextButton
import com.syme.ui.component.text.TextWithBackground
import com.syme.ui.theme.GreenTank
import com.syme.ui.theme.RedTank
import com.syme.ui.theme.YellowTank
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

    val totalEnergy_kWhRemaining = consumption.totalEnergy_kWh - consumption.totalEnergy_kWhConsummed

    // Niveau d'énergie
    val energyLevel = (totalEnergy_kWhRemaining / consumption.totalEnergy_kWh)
        .coerceIn(0.0, 1.0).toFloat()

    // Couleur dynamique selon niveau (même logique que TankLevelIndicator)
    val energyColor = when {
        energyLevel > 0.5f -> GreenTank
        energyLevel > 0.3f -> YellowTank
        else -> RedTank
    }
    Card(
        modifier = modifier
            .then(
                if (cardWidth != Dp.Unspecified && cardHeight != Dp.Unspecified) {
                    Modifier.size(width = cardWidth, height = cardHeight)
                } else Modifier.fillMaxWidth()
            )
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header: ID à gauche, état à droite
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = consumption.consumptionId,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = dynamicState.name,
                    color = when (dynamicState) {
                        ConsumptionStateType.RUNNING -> MaterialTheme.colorScheme.primary
                        ConsumptionStateType.WAITING -> MaterialTheme.colorScheme.onSurfaceVariant
                        ConsumptionStateType.COMPLETED -> MaterialTheme.colorScheme.secondary
                        ConsumptionStateType.PAUSED -> MaterialTheme.colorScheme.tertiary
                        ConsumptionStateType.ERROR -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // TankLevelIndicator à gauche
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "$totalEnergy_kWhRemaining kWh",
                        fontWeight = FontWeight.Bold,
                        color = energyColor
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    TankLevelIndicator(
                        level = energyLevel,
                        width = 80.dp,
                        height = 150.dp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Infos à droite
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.Top),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    InfoItem(
                        id = R.drawable.outline_home_24,
                        title = "installation ID",
                        value = consumption.installationId ?: "-"
                    )
                    InfoItem(
                        id = R.drawable.outline_electric_bolt_24,
                        title = "Meter ID",
                        value = consumption.meterId ?: "-"
                    )
                    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
                    val periodStartText = dateFormat.format(Date(consumption.periodStart))
                    val periodEndText = dateFormat.format(Date(consumption.periodEnd))
                    InfoItem(
                        id = R.drawable.outline_schedule_24,
                        title = "Period",
                        value = "$periodStartText → $periodEndText"
                    )

                    CountdownTimer(endTime = consumption.periodEnd, textColor = energyColor)

                }
            }

            // Switch pause
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                AppSwitch(
                    checked = isPaused,
                    onCheckedChange = {
                        isPaused = it
                        onPauseToggle(it)
                    },
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
            }
        }
    }
}

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
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun CountdownTimer(endTime: Long, textColor: Color) {
    var remainingTime by remember { mutableStateOf(endTime - System.currentTimeMillis()) }

    LaunchedEffect(endTime) {
        while (remainingTime > 0) {
            remainingTime = endTime - System.currentTimeMillis()
            delay(1000)
        }
    }

    val duration = remainingTime.coerceAtLeast(0L).toDuration(DurationUnit.MILLISECONDS)
    val days = duration.inWholeDays
    val hours = (duration.inWholeHours % 24)
    val minutes = (duration.inWholeMinutes % 60)
    val seconds = (duration.inWholeSeconds % 60)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        TextWithBackground(
            text = String.format("%dJ %02dh %02dm %02ds", days, hours, minutes, seconds),
            color = textColor
        )
    }
}

@Composable
fun ConsumptionColumn(
    consumptions: List<Consumption>,
    onPauseToggle: (Consumption, Boolean) -> Unit,
    cardWidth: Dp = Dp.Unspecified,
    cardHeight: Dp = Dp.Unspecified
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (cardHeight != Dp.Unspecified) cardHeight else 300.dp)
    ) {
        if (consumptions.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn (
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(consumptions) { cons ->
                    ConsumptionCard(
                        consumption = cons,
                        cardWidth = cardWidth,
                        cardHeight = cardHeight,
                        onPauseToggle = { paused ->
                            onPauseToggle(cons, paused)
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConsumptionCardStatesPreview() {
    val now = System.currentTimeMillis()
    val oneHour = 3600_000L

    val consumptions = listOf(
        // WAITING : période dans le futur
        Consumption(
            consumptionId = "C001",
            installationId = "H001",
            meterId = "M001",
            periodStart = now + oneHour,
            periodEnd = now + 2 * oneHour,
            totalEnergy_kWh = 100.0,
            totalEnergy_kWhConsummed = 100.0,
            consumptionState = ConsumptionStateType.WAITING
        ),
        // RUNNING : période en cours
        Consumption(
            consumptionId = "C002",
            installationId = "H002",
            meterId = "M002",
            periodStart = now - oneHour,
            periodEnd = now + oneHour,
            totalEnergy_kWh = 100.0,
            totalEnergy_kWhConsummed = 60.0,
            consumptionState = ConsumptionStateType.RUNNING
        ),
        // COMPLETED : période terminée
        Consumption(
            consumptionId = "C003",
            installationId = "H003",
            meterId = "M003",
            periodStart = now - 3 * oneHour,
            periodEnd = now - 2 * oneHour,
            totalEnergy_kWh = 100.0,
            totalEnergy_kWhConsummed = 0.0,
            consumptionState = ConsumptionStateType.COMPLETED
        ),
        // PAUSED : manuel
        Consumption(
            consumptionId = "C004",
            installationId = "H004",
            meterId = "M004",
            periodStart = now - oneHour,
            periodEnd = now + oneHour,
            totalEnergy_kWh = 100.0,
            totalEnergy_kWhConsummed = 40.0,
            consumptionState = ConsumptionStateType.PAUSED
        ),
        // ERROR : exemple d’erreur
        Consumption(
            consumptionId = "C005",
            installationId = "H005",
            meterId = "M005",
            periodStart = now - oneHour,
            periodEnd = now + oneHour,
            totalEnergy_kWh = 100.0,
            totalEnergy_kWhConsummed = 20.0,
            consumptionState = ConsumptionStateType.ERROR
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        consumptions.forEach { cons ->
            ConsumptionCard(
                consumption = cons,
                onPauseToggle = { /* noop */ }
            )
        }
    }
}
