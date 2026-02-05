package com.syme.ui.component.actionbutton

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syme.ui.theme.SYMETheme

@Composable
fun FilterChipItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,

    selectedBackgroundColor: Color,
    selectedBorderColor: Color,      // 👈 plus foncé
    unselectedBackgroundColor: Color,
    unselectedTextColor: Color,
    selectedTextColor: Color,

    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(50),
        color = if (selected) selectedBackgroundColor else unselectedBackgroundColor,
        border = BorderStroke(
            3.dp,
            if (selected) selectedBorderColor else unselectedTextColor
        )
    ) {
        Text(
            text = text,
            color = if (selected) selectedTextColor else unselectedTextColor,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            fontWeight = FontWeight.Bold,
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