package com.syme.ui.component.text

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.R

@Composable
fun Title(
    title: String,
    fontSize: Int = 24,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground
) {
    Text(
        text = title,
        fontSize = fontSize.sp,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 25.dp, top = 15.dp),
        textAlign = TextAlign.Start,
        color = color,
        fontWeight = FontWeight.ExtraBold
    )
}