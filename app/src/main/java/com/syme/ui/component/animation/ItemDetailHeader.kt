package com.syme.ui.component.animation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.syme.R
import com.syme.ui.component.text.Title

@Composable
fun ItemDetailHeader(
    id: Int,
    label: String? = null,
    onBack: () -> Unit
) {
    val bgTop = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    val bgBottom = MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
            .background(
                Brush.verticalGradient(
                    listOf(bgTop, bgBottom)
                )
            )
            .padding(top = 24.dp),
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Title(
                title = label ?: "",
                onBackClick = onBack
            )

            Spacer(modifier = Modifier.height(16.dp)) // 👈 espace entre titre et image

            AsyncImage(
                model = id,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // 👈 prend le reste de l’espace
                    .graphicsLayer {
                        scaleX = 1.05f
                        scaleY = 1.05f
                    }
            )
        }
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