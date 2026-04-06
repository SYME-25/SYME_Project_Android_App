package com.syme.ui.component.tank

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.ui.theme.TankGreen
import com.syme.ui.theme.TankRed
import com.syme.ui.theme.TankYellow

@Composable
fun TankLevelIndicator(
    level: Float,
    width: Dp = 56.dp,
    height: Dp = 130.dp,
    cornerRadius: Dp = 10.dp
) {
    val animatedLevel by animateFloatAsState(
        targetValue = level.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 700),
        label = "tankLevel"
    )

    val fillColor: Color = when {
        animatedLevel > 0.5f -> TankGreen
        animatedLevel > 0.3f -> TankYellow
        else                 -> TankRed
    }

    // Outer track
    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Fill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(animatedLevel)
                .clip(RoundedCornerShape(cornerRadius))
                .background(fillColor.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            if (animatedLevel > 0.18f) {
                Text(
                    text = "${(animatedLevel * 100).toInt()}%",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
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
