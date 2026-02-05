package com.syme.ui.screen.consumption

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.syme.R
import com.syme.domain.model.enumeration.PeriodFilter

@Composable
fun PeriodFilterSegmented(
    selected: PeriodFilter,
    onSelectedChange: (PeriodFilter) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 40.dp
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(2.dp)
    ) {
        PeriodFilterItem(
            label = stringResource(R.string.consumption_label_year),
            selected = selected == PeriodFilter.YEAR,
            onClick = { onSelectedChange(PeriodFilter.YEAR) },
            modifier = Modifier.weight(1f)
        )

        PeriodFilterItem(
            label = stringResource(R.string.consumption_label_month),
            selected = selected == PeriodFilter.MONTH,
            onClick = { onSelectedChange(PeriodFilter.MONTH) },
            modifier = Modifier.weight(1f)
        )

        PeriodFilterItem(
            label = stringResource(R.string.consumption_label_day),
            selected = selected == PeriodFilter.DAY,
            onClick = { onSelectedChange(PeriodFilter.DAY) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PeriodFilterItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (selected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PeriodFilterSegmentedPreview() {
    var selected by remember { mutableStateOf(PeriodFilter.MONTH) }

    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            PeriodFilterSegmented(
                selected = selected,
                onSelectedChange = { selected = it }
            )
        }
    }
}
