package com.syme.ui.screen.consumption

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.syme.R
import coil.compose.AsyncImage

@Composable
fun BannerConsumption(){
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .padding(top= 24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        PulsingImage()
    }
}

@Composable
fun PulsingImage() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scaleAnim"
    )

    AsyncImage(
        model = R.drawable.save_energy_hourglass_svgrepo_com,
        contentDescription = "Banner",
        modifier = Modifier
            .size(100.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        contentScale = ContentScale.Crop
    )
}
