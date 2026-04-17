package com.syme.ui.component.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.R
import com.syme.ui.component.actionbutton.FilterChipItem

@Composable
fun <T> FilterSection(
    title: String,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T?) -> Unit,
    itemLabel: @Composable (T) -> String,
    showAll: Boolean = true,
    allLabel: String = stringResource(R.string.all)
) {
    val colorPrimary = MaterialTheme.colorScheme.primary
    val colorOnPrimary = MaterialTheme.colorScheme.onPrimary
    val colorSurfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val colorOnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {

        // ── Title ─────────────────────────────
        if (title.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colorPrimary)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    letterSpacing = 0.4.sp
                )
            }
        }

        // ── Chips ─────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (showAll) {
                FilterChipItem(
                    text = allLabel,
                    selected = selectedItem == null,
                    onClick = { onItemSelected(null) },
                    selectedBackgroundColor = colorPrimary,
                    selectedBorderColor = colorPrimary,
                    selectedTextColor = colorOnPrimary,
                    unselectedBackgroundColor = colorSurfaceVariant,
                    unselectedTextColor = colorOnSurfaceVariant
                )
            }

            items.forEach { item ->
                FilterChipItem(
                    text = itemLabel(item),
                    selected = item == selectedItem,
                    onClick = { onItemSelected(item) },
                    selectedBackgroundColor = colorPrimary,
                    selectedBorderColor = colorPrimary,
                    selectedTextColor = colorOnPrimary,
                    unselectedBackgroundColor = colorSurfaceVariant,
                    unselectedTextColor = colorOnSurfaceVariant
                )
            }
        }
    }
}