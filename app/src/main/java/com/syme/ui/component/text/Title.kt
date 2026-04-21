package com.syme.ui.component.text

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.ui.component.actionbutton.AppIconButton

@Composable
fun Title(
    title: String,
    fontSize: Int = 32,
    modifier: Modifier = Modifier,
    padding: Int = 16,
    color: Color = MaterialTheme.colorScheme.onBackground,
    onBackClick: (() -> Unit)? = null,
    centered: Boolean = false // 👈 nouveau paramètre
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = padding.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (centered) {
            // 🔥 MODE CENTRÉ (uniquement si activé)
            if (onBackClick != null) {
                AppIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBackClick
                )
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }

            Text(
                text = title,
                fontSize = fontSize.sp,
                color = color,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.size(48.dp))
        } else {
            // 🧘 MODE NORMAL (ton comportement actuel)
            if (onBackClick != null) {
                AppIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBackClick,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            Text(
                text = title,
                fontSize = fontSize.sp,
                color = color,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp,
                lineHeight = (fontSize * 1.1).sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}