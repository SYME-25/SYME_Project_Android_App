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
import com.syme.domain.mapper.allowedApplianceTypes
import com.syme.domain.mapper.labelResId
import com.syme.domain.model.enumeration.ApplianceType
import com.syme.domain.model.enumeration.InstallationType
import com.syme.ui.component.actionbutton.FilterChipItem

@Composable
fun ApplianceFilterByInstallationType(
    title: String,
    selectedInstallationType: InstallationType?,
    selectedApplianceType: ApplianceType?,
    onApplianceTypeSelected: (ApplianceType?) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    val applianceTypes = selectedInstallationType
        ?.allowedApplianceTypes
        ?: ApplianceType.entries

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

            // 🟢 Tous
            item {
                FilterChipItem(
                    text = stringResource(R.string.home_installation_filter_all),
                    selected = selectedApplianceType == null,
                    onClick = { onApplianceTypeSelected(null) },

                    selectedBackgroundColor = colors.secondaryContainer,
                    selectedBorderColor = colors.secondary,
                    selectedTextColor = colors.onSecondaryContainer,

                    unselectedBackgroundColor = colors.surface,
                    unselectedTextColor = colors.onSurface
                )
            }

            // 🔹 Chips dynamiques selon le type d’installation
            items(applianceTypes) { type ->
                FilterChipItem(
                    text = stringResource(type.labelResId),
                    selected = type == selectedApplianceType,
                    onClick = { onApplianceTypeSelected(type) },

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
