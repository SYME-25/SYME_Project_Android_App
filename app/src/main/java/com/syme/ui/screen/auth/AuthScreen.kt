package com.syme.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.ui.component.animation.banner.Banner
import com.syme.ui.component.text.Title

@Composable
fun AuthScreen(
    content: @Composable (PaddingValues) -> Unit
) {
    // Pas de Scaffold, pas de TopAppBar — on gère nous-mêmes les insets
    content(WindowInsets.systemBars.asPaddingValues())
}

@Composable
fun AuthBackground(
    contentPadding: PaddingValues,
    content: @Composable BoxScope.() -> Unit
) {
    val bgTop    = MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)
        .compositeOver(MaterialTheme.colorScheme.background)
    val bgBottom = MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Le dégradé couvre TOUTE la hauteur écran, status bar incluse
            .background(Brush.verticalGradient(listOf(bgTop, bgBottom, bgBottom)))
            // Puis on applique le padding des barres système pour le contenu
            .padding(contentPadding),
        content = content
    )
}

// Bloc animation + titres
@Composable
fun AuthHeader(
    animationRes: Int,
    title: String,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null
) {

    Banner(id = animationRes)

    Title(
        title = title,
        color = MaterialTheme.colorScheme.onBackground,
        centered = true,
        onBackClick = onBackClick
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