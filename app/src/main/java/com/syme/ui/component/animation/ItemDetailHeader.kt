package com.syme.ui.component.animation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.syme.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailHeader(
    id: Int,
    onBack: () -> Unit
){
    Box(
        modifier = Modifier
        .fillMaxWidth()
        .height(500.dp)
        .background(color = MaterialTheme.colorScheme.background)
        .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .padding(start = 16.dp, top = 8.dp)
                .size(40.dp) // taille du cercle
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        AsyncImage(
            model = id,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize().align(Alignment.TopCenter)
        )
    }
}

@Preview
@Composable
fun ItemDetailHeaderPreview(){
    ItemDetailHeader(
        id = R.drawable.immeuble_de_bureaux,
        onBack = {}
    )
}