package com.syme.ui.screen.installation

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.syme.domain.mapper.labelResId
import com.syme.domain.model.enumeration.InstallationType
import com.syme.ui.component.actionbutton.FilterChipItem
import com.syme.ui.component.text.Title

@Composable
fun InstallationTypeFilterByType(
    title: String,
    selectedType: InstallationType?,
    onTypeSelected: (InstallationType?) -> Unit
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
                selected = selectedType == null,
                onClick = { onTypeSelected(null) },

                selectedBackgroundColor = colorPrimary,
                selectedBorderColor = colorPrimary,
                selectedTextColor = Color.White,

                unselectedBackgroundColor = colorAccentBg,
                unselectedTextColor = Color(0xFF5C6BC0)
            )

            // 🔹 Chips enum
            InstallationType.entries.forEach { type ->
                FilterChipItem(
                    text = stringResource(type.labelResId),
                    selected = type == selectedType,
                    onClick = { onTypeSelected(type) },

                    selectedBackgroundColor = colorPrimary,
                    selectedBorderColor = colorPrimary,
                    selectedTextColor = Color.White,

                    unselectedBackgroundColor = colorAccentBg,
                    unselectedTextColor = Color(0xFF5C6BC0)
                )
            }
        }
    }
}