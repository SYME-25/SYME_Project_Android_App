package com.syme.ui.screen.appliance

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import com.syme.domain.mapper.labelResId
import com.syme.domain.model.enumeration.ApplianceHeatType
import com.syme.ui.component.actionbutton.FilterChipItem

@Composable
fun ApplianceHeatTypeFilter(
    title: String,
    selectedHeatType: ApplianceHeatType?,
    onHeatTypeSelected: (ApplianceHeatType?) -> Unit
) {
    val colorPrimary = MaterialTheme.colorScheme.primary
    val colorOnPrimary = MaterialTheme.colorScheme.onPrimary
    val colorSurfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val colorOnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {

        if (title.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                // Accent bar
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colorPrimary)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorPrimary,
                    letterSpacing = 0.2.sp
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 🔹 Chip ALL
            FilterChipItem(
                text = stringResource(R.string.home_installation_filter_all),
                selected = selectedHeatType == null,
                onClick = { onHeatTypeSelected(null) },
                selectedBackgroundColor = colorPrimary,
                selectedBorderColor = colorPrimary,
                selectedTextColor = colorOnPrimary,
                unselectedBackgroundColor = colorSurfaceVariant,
                unselectedTextColor = colorOnSurfaceVariant
            )

            // 🔹 Chips enum
            ApplianceHeatType.entries.forEach { heatType ->
                FilterChipItem(
                    text = stringResource(heatType.labelResId),
                    selected = heatType == selectedHeatType,
                    onClick = { onHeatTypeSelected(heatType) },
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