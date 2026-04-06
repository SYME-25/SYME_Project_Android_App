package com.syme.ui.component.text

import androidx.compose.foundation.layout.Box
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
    fontSize: Int = 32,
    modifier: Modifier = Modifier,
    padding: Int = 16,
    color: Color = MaterialTheme.colorScheme.onBackground
) {
    Text(
        text = title,
        fontSize = fontSize.sp,
        color = color,
        fontWeight = FontWeight.Black,
        letterSpacing = (-0.5).sp,
        lineHeight = (fontSize * 1.1).sp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = padding.dp),
        textAlign = TextAlign.Start
    )
}