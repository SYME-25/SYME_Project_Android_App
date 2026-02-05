package com.syme.ui.screen.consumption

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        AsyncImage(
            model = R.drawable.save_energy_hourglass_svgrepo_com,
            contentDescription = "Banner"
        )
    }
}