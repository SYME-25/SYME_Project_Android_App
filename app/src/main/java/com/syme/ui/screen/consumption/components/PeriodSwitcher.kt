package com.syme.ui.screen.consumption.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.domain.model.enumeration.PeriodFilter
import com.syme.utils.TimeUtils
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PeriodSwitcher(
    selectedPeriod: PeriodFilter,
    currentDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp
) {
    val primary = MaterialTheme.colorScheme.primary
    val bgAccent = MaterialTheme.colorScheme.surfaceVariant
    val labelBg = MaterialTheme.colorScheme.surface

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(14.dp))
            .background(labelBg)
            .padding(horizontal = 6.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // ← Previous
        NavArrowButton(
            icon    = Icons.AutoMirrored.Filled.ArrowBack,
            tint    = primary,
            bgColor = bgAccent,
            onClick = {
                onDateChange(
                    when (selectedPeriod) {
                        PeriodFilter.DAY   -> currentDate.minusDays(1)
                        PeriodFilter.MONTH -> currentDate.minusMonths(1)
                        PeriodFilter.YEAR  -> currentDate.minusYears(1)
                    }
                )
            }
        )

        // Center label
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(10.dp))
                .background(primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text         = TimeUtils.formatDateForPeriod(selectedPeriod, currentDate),
                color        = MaterialTheme.colorScheme.onPrimary,
                fontSize     = 13.sp,
                fontWeight   = FontWeight.SemiBold,
                letterSpacing = 0.4.sp
            )
        }

        // → Next
        NavArrowButton(
            icon    = Icons.AutoMirrored.Filled.ArrowForward,
            tint    = primary,
            bgColor = bgAccent,
            onClick = {
                onDateChange(
                    when (selectedPeriod) {
                        PeriodFilter.DAY   -> currentDate.plusDays(1)
                        PeriodFilter.MONTH -> currentDate.plusMonths(1)
                        PeriodFilter.YEAR  -> currentDate.plusYears(1)
                    }
                )
            }
        )
    }
}

@Composable
private fun NavArrowButton(
    icon: ImageVector,
    tint: Color,
    bgColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PeriodSwitcherPreview() {
    var selectedPeriod by remember { mutableStateOf(PeriodFilter.MONTH) }
    var currentDate    by remember { mutableStateOf(LocalDate.now()) }
    MaterialTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PeriodFilterSegmented(selected = selectedPeriod, onSelectedChange = { selectedPeriod = it })
            PeriodSwitcher(selectedPeriod = selectedPeriod, currentDate = currentDate, onDateChange = { currentDate = it })
        }
    }
}