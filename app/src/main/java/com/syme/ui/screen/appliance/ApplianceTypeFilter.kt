package com.syme.ui.screen.appliance

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.R
import com.syme.domain.mapper.labelResId
import com.syme.domain.model.enumeration.ApplianceType
import com.syme.ui.component.actionbutton.FilterChipItem

@Composable
fun ApplianceTypeFilter(
    title: String,
    selectedType: ApplianceType?,
    onTypeSelected: (ApplianceType?) -> Unit   // 👈 nullable pour "Tous"
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 24.dp, end = 16.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow {
            // 🔹 Chips depuis l'enum ApplianceType
            items(ApplianceType.entries) { type ->
                FilterChipItem(
                    text = stringResource(type.labelResId),
                    selected = type == selectedType,
                    onClick = { onTypeSelected(type) },

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
