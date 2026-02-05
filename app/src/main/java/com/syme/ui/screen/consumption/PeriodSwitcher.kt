package com.syme.ui.screen.consumption

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
    height: Dp = 40.dp
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ⬅️ Previous
        SwitcherButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            onClick = {
                onDateChange(
                    when (selectedPeriod) {
                        PeriodFilter.DAY -> currentDate.minusDays(1)
                        PeriodFilter.MONTH -> currentDate.minusMonths(1)
                        PeriodFilter.YEAR -> currentDate.minusYears(1)
                    }
                )
            },
            modifier = Modifier.weight(1f)
        )

        // 🟦 Center label
        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = TimeUtils.formatDateForPeriod(selectedPeriod, currentDate),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        // ➡️ Next
        SwitcherButton(
            icon = Icons.AutoMirrored.Filled.ArrowForward,
            onClick = {
                onDateChange(
                    when (selectedPeriod) {
                        PeriodFilter.DAY -> currentDate.plusDays(1)
                        PeriodFilter.MONTH -> currentDate.plusMonths(1)
                        PeriodFilter.YEAR -> currentDate.plusYears(1)
                    }
                )
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PeriodSwitcherPreview() {
    var selectedPeriod by remember { mutableStateOf(PeriodFilter.MONTH) }
    var currentDate by remember { mutableStateOf(LocalDate.now()) }

    Column(Modifier.padding(16.dp)) {
        PeriodFilterSegmented(
            selected = selectedPeriod,
            onSelectedChange = { selectedPeriod = it }
        )

        Spacer(Modifier.height(12.dp))

        PeriodSwitcher(
            selectedPeriod = selectedPeriod,
            currentDate = currentDate,
            onDateChange = { currentDate = it }
        )
    }
}


@Composable
private fun SwitcherButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}