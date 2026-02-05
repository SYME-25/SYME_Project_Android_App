package com.syme.ui.screen.consumption

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.syme.ui.component.actionbutton.FilterChipItem

@Composable
fun InstallationFilterById(
    title: String,
    installationIds: List<String>,
    selectedInstallationId: String?,
    onInstallationSelected: (String?) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 24.dp, end = 16.dp)
    ) {
        if (title.isNotEmpty()) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        LazyRow {
            // 🔹 "All" chip
            item {
                FilterChipItem(
                    text = "All",
                    selected = selectedInstallationId == null,
                    onClick = { onInstallationSelected(null) },
                    selectedBackgroundColor = colors.secondaryContainer,
                    selectedBorderColor = colors.secondary,
                    selectedTextColor = colors.onSecondaryContainer,
                    unselectedBackgroundColor = colors.surface,
                    unselectedTextColor = colors.onSurface
                )
            }

            // 🔹 Chips pour chaque installation
            items(installationIds) { id ->
                FilterChipItem(
                    text = id,
                    selected = id == selectedInstallationId,
                    onClick = { onInstallationSelected(id) },
                    selectedBackgroundColor = colors.secondaryContainer,
                    selectedBorderColor = colors.secondary,
                    selectedTextColor = colors.onSecondaryContainer,
                    unselectedBackgroundColor = colors.surface,
                    unselectedTextColor = colors.onSurface
                )
            }
        }
    }
}
