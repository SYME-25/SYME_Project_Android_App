package com.syme.ui.component.text

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Compact pill badge — no fillMaxSize, sizes to content.
 * Use for status, power, state labels.
 */
@Composable
fun EntityBadge(
    text: String,
    color: Color,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.10f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontWeight = FontWeight.SemiBold,
            style = textStyle,
            maxLines = 1
        )
    }
}

// Keep old name as alias for migration
@Composable
fun TextWithBackground(
    text: String,
    color: Color,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall,
    modifier: Modifier = Modifier
) = EntityBadge(text = text, color = color, textStyle = textStyle, modifier = modifier)