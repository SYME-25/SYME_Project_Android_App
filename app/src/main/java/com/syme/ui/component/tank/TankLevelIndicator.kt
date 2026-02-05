package com.syme.ui.component.tank

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.syme.ui.theme.GreenTank
import com.syme.ui.theme.RedTank
import com.syme.ui.theme.YellowTank

@Composable
fun TankLevelIndicator(
    level: Float,                   // 0f à 1f
    width: Dp = 100.dp,
    height: Dp = 150.dp,
    cornerRadius: Dp = 12.dp,
    padding: Dp = 6.dp,             // padding entre bord externe et niveau
    borderWidth: Dp = 4.dp          // épaisseur des bordures
) {
    // Animation douce du niveau
    val animatedLevel by animateFloatAsState(
        targetValue = level.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600)
    )

    // Couleur dynamique selon le niveau
    val fillColor = when {
        animatedLevel > 0.5f -> GreenTank // vert
        animatedLevel > 0.3f -> YellowTank // jaune
        else -> RedTank                 // rouge
    }

    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .background(MaterialTheme.colorScheme.surface) // <-- ici
            .border(width = borderWidth, color = fillColor, shape = RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Niveau de remplissage avec padding
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(animatedLevel)
                .padding(horizontal = padding, vertical = padding)
                .clip(RoundedCornerShape(cornerRadius - padding))
                .background(fillColor),
            contentAlignment = Alignment.Center
        ) {
            // Pourcentage affiché à l'intérieur
            Text(
                text = "${(animatedLevel * 100).toInt()}%",
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TankLevelIndicatorStatesPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Batterie Verte (>50%)")
        TankLevelIndicator(level = 0.8f) // Vert

        Text("Batterie Jaune (20–40%)")
        TankLevelIndicator(level = 0.4f) // Jaune

        Text("Batterie Rouge (<20%)")
        TankLevelIndicator(level = 0.2f) // Rouge
    }
}
