package com.syme.ui.component.actionbutton

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syme.ui.theme.SYMETheme

@Composable
fun FilterChipItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    selectedBackgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    selectedBorderColor: Color = MaterialTheme.colorScheme.primary,
    selectedTextColor: Color = MaterialTheme.colorScheme.primary,
    unselectedBackgroundColor: Color = MaterialTheme.colorScheme.surface,
    unselectedTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) selectedBackgroundColor else unselectedBackgroundColor,
        animationSpec = tween(200),
        label = "chipBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) selectedTextColor else unselectedTextColor,
        animationSpec = tween(200),
        label = "chipText"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) selectedBorderColor else MaterialTheme.colorScheme.outline,
        animationSpec = tween(200),
        label = "chipBorder"
    )

    Surface(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .clickable(role = Role.Button) { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = text,
            color = textColor,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FilterChipItemSelectedPreview() {
    SYMETheme {
        FilterChipItem(
            text = "Non lus",
            selected = true,
            onClick = {},

            selectedBackgroundColor = Color(0xFFD9FDD3), // vert clair
            selectedBorderColor = Color(0xFF1FA855),     // vert plus foncé
            selectedTextColor = Color(0xFF1FA855),

            unselectedBackgroundColor = MaterialTheme.colorScheme.surface,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FilterChipItemUnselectedPreview() {
    SYMETheme {
        FilterChipItem(
            text = "Non lus",
            selected = false,
            onClick = {},

            selectedBackgroundColor = Color(0xFFD9FDD3), // vert clair
            selectedBorderColor = Color(0xFF1FA855),     // vert plus foncé
            selectedTextColor = Color(0xFF1FA855),

            unselectedBackgroundColor = MaterialTheme.colorScheme.surface,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface
        )
    }
}