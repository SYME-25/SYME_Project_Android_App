package com.syme.ui.screen.consumption

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.R
import com.syme.domain.model.enumeration.PeriodFilter

@Composable
fun PeriodFilterSegmented(
    selected: PeriodFilter,
    onSelectedChange: (PeriodFilter) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 44.dp
) {
    val colorPrimary = Color(0xFF1A237E)
    val colorBg      = Color(0xFFECEFFE)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(colorBg)
            .padding(3.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            PeriodFilter.entries.forEach { filter ->
                val label = when (filter) {
                    PeriodFilter.YEAR  -> stringResource(R.string.consumption_label_year)
                    PeriodFilter.MONTH -> stringResource(R.string.consumption_label_month)
                    PeriodFilter.DAY   -> stringResource(R.string.consumption_label_day)
                }
                PeriodFilterItem(
                    label    = label,
                    selected = selected == filter,
                    onClick  = { onSelectedChange(filter) },
                    modifier = Modifier.weight(1f),
                    selectedColor = colorPrimary
                )
            }
        }
    }
}

@Composable
private fun PeriodFilterItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) selectedColor else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "segBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Color.White else Color(0xFF5C6BC0),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "segText"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(9.dp))
            .then(
                if (selected) Modifier.shadow(2.dp, RoundedCornerShape(9.dp)) else Modifier
            )
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = label,
            color      = textColor,
            fontSize   = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            letterSpacing = 0.3.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PeriodFilterSegmentedPreview() {
    var selected by remember { mutableStateOf(PeriodFilter.MONTH) }
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            PeriodFilterSegmented(selected = selected, onSelectedChange = { selected = it })
        }
    }
}