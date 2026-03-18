package com.syme.ui.screen.consumption

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.R
import com.syme.ui.component.actionbutton.FilterChipItem

@Composable
fun InstallationFilterById(
    title: String,
    installationIds: List<String>,
    selectedInstallationId: String?,
    onInstallationSelected: (String?) -> Unit
) {
    val colorPrimary  = Color(0xFF1A237E)
    val colorAccentBg = Color(0xFFECEFFE)

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
                    text       = title,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color      = colorPrimary,
                    letterSpacing = 0.2.sp
                )
            }
        }

        if (installationIds.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                installationIds.forEach { id ->
                    val isSelected = id == selectedInstallationId
                    FilterChipItem(
                        text     = id,
                        selected = isSelected,
                        onClick  = { onInstallationSelected(id) },
                        selectedBackgroundColor   = colorPrimary,
                        selectedBorderColor       = colorPrimary,
                        selectedTextColor         = Color.White,
                        unselectedBackgroundColor = colorAccentBg,
                        unselectedTextColor       = Color(0xFF5C6BC0)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(colorAccentBg)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text      = stringResource(R.string.home_no_installations_found),
                    fontSize  = 13.sp,
                    color     = Color(0xFF5C6BC0),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}