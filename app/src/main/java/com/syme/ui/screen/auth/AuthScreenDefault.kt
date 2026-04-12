package com.syme.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.ui.component.animation.banner.Banner

// Fond dégradé commun à tous les screens auth
@Composable
fun AuthBackground(content: @Composable BoxScope.() -> Unit) {
    val bgTop    = MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)
    val bgBottom = MaterialTheme.colorScheme.background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(bgTop, bgBottom, bgBottom))),
        content = content
    )
}

// Bloc animation + titres
@Composable
fun AuthHeader(
    animationRes: Int,
    title: String,
    subtitle: String? = null
) {
    Banner(id = animationRes,)
    Text(
        text = title,
        fontSize = 26.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 4.dp, bottom = if (subtitle != null) 2.dp else 20.dp)
    )
    if (subtitle != null) {
        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 20.dp)
        )
    }
}

// Carte semi-transparente qui regroupe les champs
@Composable
fun AuthFieldsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

// Séparateur de section avec label
@Composable
fun AuthSectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
        modifier = Modifier.padding(start = 28.dp, top = 8.dp, bottom = 2.dp)
    )
}