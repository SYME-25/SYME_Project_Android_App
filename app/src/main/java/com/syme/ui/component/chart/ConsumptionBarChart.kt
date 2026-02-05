package com.syme.ui.component.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.syme.domain.model.ConsumptionBar
import com.syme.ui.theme.YellowTank
import com.syme.ui.theme.darkBlue
import com.syme.ui.theme.lightBlue
import kotlin.math.roundToInt

@Composable
fun ConsumptionInjectionBarChart(
    data: List<ConsumptionBar>,
    injection: List<Float>,
    modifier: Modifier = Modifier,
    maxHeight: Dp = 220.dp,
    barWidth: Dp = 25.dp,
    barSpacing: Dp = 6.dp,
    ySteps: Int = 4,
    yValueFormatter: (Float) -> String = { it.roundToInt().toString() },
    xLabelStep: Int = 2,
    moneyUnit: String = "FCFA",
    energyUnit: String = "kWh",
    kWhPrice: Float = 49.0f
) {
    val maxValueTop = data.maxOfOrNull { it.subscription + it.consumption } ?: 1f
    val maxValueBottom = injection.maxOrNull() ?: 1f

    val lastConsumptionValue by remember(data) {
        derivedStateOf {
            data.lastOrNull()?.let { it.subscription + it.consumption } ?: 0f
        }
    }

    val lastInjectionValue by remember(injection) {
        derivedStateOf {
            injection.lastOrNull() ?: 0f
        }
    }



    Column(modifier) {

        ConsumptionSummary(
            amountText = "${(lastConsumptionValue * kWhPrice).roundToInt()} $moneyUnit", // exemple conversion en devise
            consumptionText = "${lastConsumptionValue.roundToInt()} $energyUnit",
            color = lightBlue,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ConsumptionSummary(
            amountText = "${(lastInjectionValue * kWhPrice).roundToInt()} $moneyUnit", // exemple conversion en devise
            consumptionText = "${lastInjectionValue.roundToInt()} $energyUnit",
            color = YellowTank,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(maxHeight)
        ) {
            val axisPadding = 48f
            val barWidthPx = barWidth.toPx()
            val barSpacingPx = barSpacing.toPx()
            val startOffset = axisPadding + barSpacingPx

            // 🔽 ARRONDI PLUS DISCRET
            val radius = 4.dp.toPx()

            val yTextPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 26f
                textAlign = android.graphics.Paint.Align.RIGHT
                isAntiAlias = true
            }

            // Axe Y haut
            repeat(ySteps + 1) { step ->
                val ratio = step / ySteps.toFloat()
                val value = maxValueTop * ratio
                val y = size.height / 2 - ratio * size.height / 2

                drawLine(
                    color = Color(0xFFE0E0E0),
                    start = Offset(axisPadding, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )

                drawContext.canvas.nativeCanvas.drawText(
                    yValueFormatter(value),
                    axisPadding - 10f,
                    y + 9f,
                    yTextPaint
                )
            }

            // Axe Y bas
            repeat(ySteps + 1) { step ->
                val ratio = step / ySteps.toFloat()
                val value = maxValueBottom * ratio
                val y = size.height / 2 + ratio * size.height / 2

                drawLine(
                    color = Color(0xFFE0E0E0),
                    start = Offset(axisPadding, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )

                drawContext.canvas.nativeCanvas.drawText(
                    yValueFormatter(value),
                    axisPadding - 10f,
                    y + 9f,
                    yTextPaint
                )
            }

            // CONSOMMATION + ABONNEMENT
            data.forEachIndexed { index, bar ->
                val total = bar.subscription + bar.consumption
                val barHeight = (total / maxValueTop) * (size.height / 2)

                val subHeight = 12.dp.toPx()

                val x = startOffset + index * (barWidthPx + barSpacingPx)
                val topY = size.height / 2 - barHeight

                // abonnement (PAS ARRONDI)
                drawRect(
                    color = darkBlue,
                    topLeft = Offset(x, size.height / 2 - subHeight),
                    size = Size(barWidthPx, subHeight)
                )

                // consommation (ARRONDI EN HAUT SEULEMENT)
                val path = Path().apply {
                    moveTo(x, size.height / 2 - subHeight)
                    lineTo(x, topY + radius)

                    arcTo(
                        rect = Rect(x, topY, x + radius * 2, topY + radius * 2),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )

                    lineTo(x + barWidthPx - radius, topY)

                    arcTo(
                        rect = Rect(
                            x + barWidthPx - radius * 2,
                            topY,
                            x + barWidthPx,
                            topY + radius * 2
                        ),
                        startAngleDegrees = 270f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )

                    lineTo(x + barWidthPx, size.height / 2 - subHeight)
                    close()
                }

                drawPath(path, lightBlue)
            }

            // INJECTION (ARRONDI EN HAUT SEULEMENT, PROPRE)
            injection.forEachIndexed { index, value ->
                val barHeight = (value / maxValueBottom) * (size.height / 2)

                val x = startOffset + index * (barWidthPx + barSpacingPx)
                val bottomY = size.height / 2
                val topY = bottomY + barHeight

                val path = Path().apply {
                    moveTo(x, bottomY)
                    lineTo(x, topY - radius)

                    // coin haut gauche
                    arcTo(
                        rect = Rect(x, topY - radius * 2, x + radius * 2, topY),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = -90f,
                        forceMoveTo = false
                    )

                    lineTo(x + barWidthPx - radius, topY)

                    // coin haut droit
                    arcTo(
                        rect = Rect(
                            x + barWidthPx - radius * 2,
                            topY - radius * 2,
                            x + barWidthPx,
                            topY
                        ),
                        startAngleDegrees = 90f,
                        sweepAngleDegrees = -90f,
                        forceMoveTo = false
                    )

                    lineTo(x + barWidthPx, bottomY)
                    close()
                }

                drawPath(path, YellowTank)
            }

        }

        // Axe X
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
        ) {
            val axisPadding = 48f
            val barWidthPx = barWidth.toPx()
            val barSpacingPx = barSpacing.toPx()
            val startOffset = axisPadding + barSpacingPx

            val xTextPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 26f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }

            data.forEachIndexed { index, bar ->
                if (index % xLabelStep == 0) {
                    val x = startOffset + index * (barWidthPx + barSpacingPx) + barWidthPx / 2
                    drawContext.canvas.nativeCanvas.drawText(
                        bar.timeLabel,
                        x,
                        size.height - 2f,
                        xTextPaint
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ChartLegendItem(color = lightBlue, label = "Consumption")
        ChartLegendItem(color = darkBlue, label = "Subscription")
        ChartLegendItem(color = YellowTank, label = "Injection")
    }
}

@Composable
fun ChartLegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(end = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

@Composable
fun ConsumptionSummary(
    amountText: String,      // ex: "12 500 FCFA"
    consumptionText: String, // ex: "18.4 kWh"
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .weight(1f)
                .background(color)

        )

        // 💰 Montant (gauche)
        Text(
            text = amountText,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )

        // │ Divider central
        VerticalDivider(
            modifier = Modifier
                .height(20.dp)
                .padding(horizontal = 4.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // ⚡ Consommation (droite)
        Text(
            text = consumptionText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ConsumptionInjectionBarChartPreview() {
    val sampleData = listOf(
        ConsumptionBar("01", 18f, 22f),
        ConsumptionBar("02", 18f, 35f),
        ConsumptionBar("03", 18f, 12f),
        ConsumptionBar("04", 18f, 45f)
    )
    val sampleInjection = listOf(5f, 12f, 8f, 20f)

    Column(
        modifier = Modifier.padding(16.dp),
    ) {
        ConsumptionInjectionBarChart(
            data = sampleData,
            injection = sampleInjection,
            maxHeight = 220.dp,
            yValueFormatter = { "${it.roundToInt()} kWh" },
            xLabelStep = 1
        )
    }
}
