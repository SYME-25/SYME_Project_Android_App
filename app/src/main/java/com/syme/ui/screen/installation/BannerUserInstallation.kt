package com.syme.ui.screen.installation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun BannerUserInstallation(id: Int){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
            .background(color = MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        AsyncImage(
            model = id,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize().align(Alignment.TopCenter)
        )
    }
}