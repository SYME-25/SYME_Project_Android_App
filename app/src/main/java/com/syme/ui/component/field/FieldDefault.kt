package com.syme.ui.component.field

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.syme.domain.model.FieldColors

// ─── Helpers ──────────────────────────────────────────────────────────────────

fun Color.isLight(): Boolean {
    val r = red * 0.299f
    val g = green * 0.587f
    val b = blue * 0.114f
    return (r + g + b) > 0.5f
}

// ─── Factory — 100% MaterialTheme, aucune couleur codée en dur ────────────────

@Composable
fun rememberFieldColors(
    isError: Boolean = false,
    isFocused: Boolean = false
): FieldColors {
    val scheme = MaterialTheme.colorScheme
    val isDark = !scheme.background.isLight()

    // Container : surfaceVariant en light, un ton au dessus du surface en dark
    val containerTarget = scheme.surfaceVariant

    val containerColor by animateColorAsState(
        targetValue = containerTarget,
        animationSpec = tween(250),
        label = "fieldContainer"
    )

    val borderTarget = when {
        isError   -> scheme.error
        isFocused -> scheme.primary
        else      -> scheme.outline.copy(alpha = 0.5f)
    }
    val borderColor by animateColorAsState(
        targetValue = borderTarget,
        animationSpec = tween(250),
        label = "fieldBorder"
    )

    val iconTarget = when {
        isError   -> scheme.error
        isFocused -> scheme.primary
        else      -> scheme.onSurfaceVariant
    }
    val iconColor by animateColorAsState(
        targetValue = iconTarget,
        animationSpec = tween(250),
        label = "fieldIcon"
    )

    val labelColor = when {
        isError -> scheme.error
        else    -> scheme.onSurfaceVariant
    }

    return FieldColors(
        container = containerColor,
        border    = borderColor,
        icon      = iconColor,
        label     = labelColor,
        text      = scheme.onSurface,
        cursor    = scheme.primary,
        isDark    = isDark
    )
}

// ─── Modifier helper ──────────────────────────────────────────────────────────

val FieldShape = RoundedCornerShape(12.dp)

fun Modifier.fieldBase(colors: FieldColors, borderWidth: Dp = 1.dp): Modifier = this
    .clip(FieldShape)
    .background(colors.container)
    .border(borderWidth, colors.border, FieldShape)

// ─── Floating label decoration ────────────────────────────────────────────────

@Composable
fun FieldDecorationBox(
    value: String,
    isFocused: Boolean,
    label: String,
    labelColor: Color,
    innerTextField: @Composable () -> Unit
) {
    val isCollapsed = isFocused || value.isNotEmpty()

    val progress by animateFloatAsState(
        targetValue = if (isCollapsed) 0f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "labelFloat"
    )

    val labelSize = lerp(11.sp, 15.sp, progress)

    val animatedLabelColor by animateColorAsState(
        targetValue = if (isCollapsed) labelColor else labelColor.copy(alpha = 0.55f),
        animationSpec = tween(180),
        label = "labelColor"
    )

    Box(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = label,
            color = animatedLabelColor,
            fontSize = labelSize,
            fontWeight = if (isCollapsed) FontWeight.Medium else FontWeight.Normal,
            letterSpacing = if (isCollapsed) 0.4.sp else 0.sp,
            modifier = Modifier.align(
                if (isCollapsed) Alignment.TopStart else Alignment.CenterStart
            )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(top = 16.dp)
        ) {
            if (isCollapsed) innerTextField()
        }
    }
}