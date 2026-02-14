package com.syme.ui.component.gaugemeter

import android.graphics.Paint
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syme.domain.model.GradientSegment
import kotlin.math.cos
import kotlin.math.sin

private const val START_ANGLE = 112.5f
private const val SWEEP_ANGLE = 315f

/* ───────────────────────────── */
/* 🌟 Fonction utilitaire angle  */
/* ───────────────────────────── */
private fun valueToAngle(value: Float, min: Float, max: Float): Float {
    val progress = ((value - min) / (max - min)).coerceIn(0f, 1f)
    return START_ANGLE + SWEEP_ANGLE * progress
}

@Composable
fun GaugeMeter(
    modifier: Modifier = Modifier,
    value: Float,
    min: Float = 0f,
    max: Float = 100f
) {
    // Animation de la valeur pour une transition fluide
    val animatedValue by animateFloatAsState(
        targetValue = value.coerceIn(min, max), // Limiter la valeur dans la plage
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "needleAnim"
    )

    // Calcul de l'angle de l'aiguille
    val needleAngle = valueToAngle(animatedValue, min, max)

    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.08f
        val radius = size.minDimension / 2f - strokeWidth
        val center = Offset(size.width / 2, size.height / 2)
        val arcTopLeft = Offset(center.x - radius, center.y - radius)
        val arcSize = Size(radius * 2, radius * 2)

        // Dessiner les éléments dans l'ordre
        drawGradientGaugeArc(center, arcTopLeft, arcSize, strokeWidth)
        drawTicks(center, radius, strokeWidth, min, max)
        drawNeedle(center, radius, strokeWidth, needleAngle)
    }

}

/* ───────────────────────────── */
/* 🌈 Arc dégradé principal      */
/* ───────────────────────────── */
private fun DrawScope.drawGradientGaugeArc(
    center: Offset,
    topLeft: Offset,
    size: Size,
    strokeWidth: Float
) {
    val segments = listOf(
        GradientSegment(0f, 0.45f, Color(0xFF2ECC71), Color(0xFF2ECC71), StrokeCap.Round),
        GradientSegment(0.45f, 0.70f, Color(0xFF2ECC71), Color(0xFFF1C40F), StrokeCap.Butt),
        GradientSegment(0.70f, 0.85f, Color(0xFFF1C40F), Color(0xFFE74C3C), StrokeCap.Butt),
        GradientSegment(0.85f, 1f, Color(0xFFE74C3C), Color(0xFFE74C3C), StrokeCap.Round)
    )

    // CORRECTION : L'arc doit être dessiné sans rotation initiale
    // car START_ANGLE est déjà pris en compte dans drawArc
    segments.forEach { segment ->
        val startAngle = START_ANGLE + SWEEP_ANGLE * segment.start
        val sweep = SWEEP_ANGLE * (segment.end - segment.start)

        // Calcul des points pour le dégradé linéaire
        val startRad = Math.toRadians(startAngle.toDouble())
        val endRad = Math.toRadians((startAngle + sweep).toDouble())

        val gradient = Brush.linearGradient(
            colors = listOf(segment.startColor, segment.endColor),
            start = Offset(
                (center.x + cos(startRad) * size.width / 2).toFloat(),
                (center.y + sin(startRad) * size.height / 2).toFloat()
            ),
            end = Offset(
                (center.x + cos(endRad) * size.width / 2).toFloat(),
                (center.y + sin(endRad) * size.height / 2).toFloat()
            )
        )

        drawArc(
            brush = gradient,
            startAngle = startAngle,
            sweepAngle = sweep,
            useCenter = false,
            style = Stroke(strokeWidth, cap = segment.cap),
            topLeft = topLeft,
            size = size
        )
    }
}

/* ───────────────────────────── */
/* 📏 Graduations                */
/* ───────────────────────────── */
private fun DrawScope.drawTicks(
    center: Offset,
    radius: Float,
    strokeWidth: Float,
    min: Float,
    max: Float
) {
    val tickCount = 20
    val tickLength = strokeWidth * 0.6f
    val textRadius = radius - strokeWidth * 1.6f

    for (i in 0..tickCount) {
        val progress = i.toFloat() / tickCount
        val angle = START_ANGLE + SWEEP_ANGLE * progress // CORRECTION : Même calcul que valueToAngle
        val rad = Math.toRadians(angle.toDouble())

        val start = Offset(
            (center.x + cos(rad) * (radius - tickLength)).toFloat(),
            (center.y + sin(rad) * (radius - tickLength)).toFloat()
        )
        val end = Offset(
            (center.x + cos(rad) * radius).toFloat(),
            (center.y + sin(rad) * radius).toFloat()
        )

        drawLine(
            color = Color.Black,
            start = start,
            end = end,
            strokeWidth = if (i % 5 == 0) 4f else 2f
        )

        if (i % 5 == 0) {
            val valueLabel = (min + (max - min) * progress).toInt()
            val textX = (center.x + cos(rad) * textRadius).toFloat()
            val textY = (center.y + sin(rad) * textRadius).toFloat()

            // Ajustement pour centrer le texte verticalement
            val paint = Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = strokeWidth * 0.6f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }

            // Mesurer la hauteur du texte pour le centrer correctement
            val textBounds = android.graphics.Rect()
            paint.getTextBounds(valueLabel.toString(), 0, valueLabel.toString().length, textBounds)

            drawContext.canvas.nativeCanvas.drawText(
                valueLabel.toString(),
                textX,
                textY + textBounds.height() / 2f,
                paint
            )
        }
    }
}

/* ───────────────────────────── */
/* 🪡 Aiguille                   */
/* ───────────────────────────── */
private fun DrawScope.drawNeedle(
    center: Offset,
    radius: Float,
    strokeWidth: Float,
    needleAngle: Float
) {
    // CORRECTION : needleAngle est déjà l'angle correct depuis START_ANGLE (112.5°)
    // Il faut juste soustraire 90° pour que 0° pointe vers le haut
    // Mais START_ANGLE = 112.5° correspond déjà à la position "min" du gauge
    // Donc on utilise directement needleAngle
    rotate(needleAngle, center) {
        val needleLength = radius * 0.9f
        val baseWidth = strokeWidth * 0.8f
        val tipWidth = strokeWidth * 0.15f

        // Dessiner l'aiguille pointant vers la droite (0°)
        // puis la rotation l'orientera correctement
        val path = Path().apply {
            moveTo(center.x, center.y - baseWidth / 2)
            lineTo(center.x, center.y + baseWidth / 2)
            lineTo(center.x + needleLength, center.y + tipWidth / 2)
            lineTo(center.x + needleLength, center.y - tipWidth / 2)
            close()
        }

        drawPath(path, Color.Red)
    }

    // Centre de l'aiguille
    drawCircle(
        color = Color.Black,
        radius = strokeWidth * 0.4f,
        center = center
    )
}

@Preview(showBackground = true)
@Composable
fun GaugeMeterPreviewAnimated() {
    val infiniteTransition = rememberInfiniteTransition(label = "gauge")

    val value by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "valueAnim"
    )

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GaugeMeter(
            modifier = Modifier.size(260.dp),
            value = value
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Valeur actuelle: ${value.toInt()}°C")
    }
}