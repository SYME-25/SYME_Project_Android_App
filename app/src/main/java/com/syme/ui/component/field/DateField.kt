package com.syme.ui.component.field

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DateField(
    value: String,
    onClick: () -> Unit,
    label: String,
    error: String,
    modifier: Modifier = Modifier
) {
    val isError = error.isNotEmpty()
    val colors = rememberFieldColors(isError = isError)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 20.dp)
            .fieldBase(colors, borderWidth = if (isError) 1.5.dp else 1.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.DateRange,
            contentDescription = null,
            tint = colors.icon,
            modifier = Modifier.size(22.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = if (isError) error else label,
                color = colors.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.4.sp
            )
            if (value.isNotEmpty()) {
                Text(
                    text = value,
                    color = colors.text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}