package com.syme.ui.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.R
import com.syme.domain.model.Appliance
import com.syme.domain.model.PowerBalance
import com.syme.ui.theme.SemanticInfo500
import com.syme.ui.theme.SemanticWarning500
import kotlin.math.sqrt

private const val NOMINAL_VOLTAGE_V = 230.0
fun computePowerBalance(appliances: List<Appliance>): PowerBalance {
    if (appliances.isEmpty()) {
        return PowerBalance(
            activePowerW = 0.0,
            reactivePowerVar = 0.0,
            apparentPowerVa = 0.0,
            currentTotalA = 0.0,
            powerFactor = 0.0
        )
    }

    val activePowerW = appliances.sumOf { it.powerWatt.toDouble() }

    if (activePowerW <= 0.0) {
        return PowerBalance(
            activePowerW = 0.0,
            reactivePowerVar = 0.0,
            apparentPowerVa = 0.0,
            currentTotalA = 0.0,
            powerFactor = 0.0
        )
    }

    val weightedPfSum = appliances.sumOf {
        it.powerWatt.toDouble() * it.powerFactor.toDouble().coerceIn(0.0, 1.0)
    }

    val cosφ = (weightedPfSum / activePowerW).coerceIn(0.001, 1.0)

    val sinφ = sqrt(1.0 - cosφ * cosφ)

    val apparentPowerVa = activePowerW / cosφ

    val reactivePowerVar = activePowerW * (sinφ / cosφ)

    val currentTotalA = apparentPowerVa / NOMINAL_VOLTAGE_V

    return PowerBalance(
        activePowerW = activePowerW,
        reactivePowerVar = reactivePowerVar,
        apparentPowerVa = apparentPowerVa,
        currentTotalA = currentTotalA,
        powerFactor = cosφ
    )
}

@Composable
fun PowerBalanceCard(
    appliances: List<Appliance>,
    modifier: Modifier = Modifier
) {
    val balance = remember(appliances) { computePowerBalance(appliances) }

    val pfColor = when {
        balance.powerFactor >= 0.90 -> MaterialTheme.colorScheme.secondary
        balance.powerFactor >= 0.75 -> SemanticWarning500
        else -> MaterialTheme.colorScheme.error
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = stringResource(R.string.power_balance_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(pfColor.copy(alpha = 0.10f))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = stringResource(
                            R.string.power_balance_pf,
                            String.format("%.2f", balance.powerFactor)
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = pfColor
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PowerMetricCell(
                        modifier = Modifier.weight(1f),
                        iconRes = R.drawable.outline_electric_bolt_24,
                        label = stringResource(R.string.power_balance_active),
                        value = formatPowerValue(balance.activePowerW, "W"),
                        accentColor = MaterialTheme.colorScheme.primary
                    )

                    PowerMetricCell(
                        modifier = Modifier.weight(1f),
                        iconRes = R.drawable.outline_electric_bolt_24,
                        label = stringResource(R.string.power_balance_reactive),
                        value = formatPowerValue(balance.reactivePowerVar, "VAR"),
                        accentColor = SemanticWarning500
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PowerMetricCell(
                        modifier = Modifier.weight(1f),
                        iconRes = R.drawable.outline_electric_bolt_24,
                        label = stringResource(R.string.power_balance_apparent),
                        value = formatPowerValue(balance.apparentPowerVa, "VA"),
                        accentColor = SemanticInfo500
                    )

                    PowerMetricCell(
                        modifier = Modifier.weight(1f),
                        iconRes = R.drawable.outline_schedule_24,
                        label = stringResource(R.string.power_balance_current),
                        value = String.format("%.2f A", balance.currentTotalA),
                        accentColor = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            PowerFactorQualityIndicator(
                powerFactor = balance.powerFactor,
                pfColor = pfColor
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = stringResource(
                    R.string.power_balance_footnote,
                    NOMINAL_VOLTAGE_V.toInt()
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun PowerMetricCell(
    modifier: Modifier = Modifier,
    iconRes: Int,
    label: String,
    value: String,
    accentColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = accentColor.copy(alpha = 0.06f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontSize = 10.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }
    }
}

private fun formatPowerValue(value: Double, unit: String): String = when {
    value >= 1_000.0 -> String.format("%.2f k%s", value / 1_000.0, unit)
    else -> String.format("%.1f %s", value, unit)
}

@Composable
private fun PowerFactorQualityIndicator(
    powerFactor: Double,
    pfColor: Color
) {
    val errorColor = MaterialTheme.colorScheme.error
    val warningColor = SemanticWarning500
    val goodColor = MaterialTheme.colorScheme.secondary

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        ) {
            val totalWidth = this.maxWidth

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(totalWidth * 0.75f)
                    .align(Alignment.CenterStart)
                    .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                    .background(errorColor.copy(alpha = 0.18f))
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(totalWidth * 0.15f)
                    .offset(x = totalWidth * 0.75f)
                    .background(warningColor.copy(alpha = 0.18f))
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(totalWidth * 0.10f)
                    .offset(x = totalWidth * 0.90f)
                    .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                    .background(goodColor.copy(alpha = 0.18f))
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(3.dp)
                    .offset(x = (totalWidth * powerFactor.toFloat()) - 1.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(pfColor)
            )
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.power_balance_pf_poor),
                modifier = Modifier.weight(0.75f),
                style = MaterialTheme.typography.labelSmall,
                color = errorColor.copy(alpha = 0.6f),
                fontSize = 9.sp
            )
            Text(
                text = stringResource(R.string.power_balance_pf_fair),
                modifier = Modifier.weight(0.15f),
                style = MaterialTheme.typography.labelSmall,
                color = warningColor.copy(alpha = 0.6f),
                fontSize = 9.sp
            )
            Text(
                text = stringResource(R.string.power_balance_pf_good),
                modifier = Modifier.weight(0.10f),
                style = MaterialTheme.typography.labelSmall,
                color = goodColor.copy(alpha = 0.6f),
                fontSize = 9.sp,
                textAlign = TextAlign.End
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.power_balance_pf_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(pfColor)
                )
                Text(
                    text = String.format("%.2f", powerFactor),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = pfColor,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
