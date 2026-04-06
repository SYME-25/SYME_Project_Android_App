package com.syme.ui.screen.installation.components

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.R
import com.syme.domain.mapper.labelResId
import com.syme.domain.model.enumeration.InstallationType
import com.syme.ui.component.actionbutton.FilterChipItem
import com.syme.ui.theme.SYMETheme

@Composable
fun InstallationTypeFilterByType(
    title: String,
    selectedType: InstallationType?,
    onTypeSelected: (InstallationType?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
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
                        .background(MaterialTheme.colorScheme.primary)
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChipItem(
                text = stringResource(R.string.home_installation_filter_all),
                selected = selectedType == null,
                onClick = { onTypeSelected(null) },
                selectedBackgroundColor = MaterialTheme.colorScheme.primary,
                selectedBorderColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                unselectedBackgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
            InstallationType.entries.forEach { type ->
                FilterChipItem(
                    text = stringResource(type.labelResId),
                    selected = type == selectedType,
                    onClick = { onTypeSelected(type) },
                    selectedBackgroundColor = MaterialTheme.colorScheme.primary,
                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                    unselectedBackgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InstallationTypeFilterByTypePreview() {
    SYMETheme {
        InstallationTypeFilterByType(
            title = "Filter by Type",
            selectedType = InstallationType.RESIDENTIAL,
            onTypeSelected = {}
        )
    }
}