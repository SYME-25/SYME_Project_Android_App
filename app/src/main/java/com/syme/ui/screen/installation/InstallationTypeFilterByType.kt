package com.syme.ui.screen.installation

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
import com.syme.domain.model.enumeration.InstallationType
import com.syme.ui.component.actionbutton.FilterChipItem
import com.syme.ui.component.text.Title

@Composable
fun InstallationTypeFilterByType(
    selectedType: InstallationType?,
    onTypeSelected: (InstallationType?) -> Unit   // 👈 nullable pour "Tous"
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 24.dp, end = 16.dp)
    ) {
        LazyRow {
            item {
                FilterChipItem(
                    text = stringResource(R.string.home_installation_filter_all),
                    selected = selectedType == null,
                    onClick = { onTypeSelected(null) },

                    selectedBackgroundColor = colors.secondaryContainer,
                    selectedBorderColor = colors.secondary,
                    selectedTextColor = colors.onSecondaryContainer,

                    unselectedBackgroundColor = colors.surface,
                    unselectedTextColor = colors.onSurface
                )
            }

            // 🔹 Chips depuis l'enum
            items(InstallationType.entries) { type ->
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
