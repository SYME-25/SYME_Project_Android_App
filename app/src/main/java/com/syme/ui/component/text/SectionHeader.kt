package com.syme.ui.component.text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syme.ui.component.actionbutton.AppIconButton
import com.syme.ui.theme.SYMETheme

@Composable
fun SectionHeader(
    title: String,
    onAddClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Title(title, 18, modifier = Modifier.weight(1f))

        AppIconButton(
            onClick = onAddClick,
            color = color
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SectionHeaderPreview() {
    SYMETheme {
        SectionHeader(
            title = "Section Header",
            onAddClick = {}
        )
    }
}
