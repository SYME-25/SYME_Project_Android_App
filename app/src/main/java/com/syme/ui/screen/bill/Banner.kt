package com.syme.ui.screen.bill

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syme.R
import com.syme.ui.component.animation.Animation

@Composable
fun BannerBill(){
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .padding(top= 24.dp),
        contentAlignment = Alignment.BottomCenter

    ) {
        Animation(R.raw.money)
    }
}