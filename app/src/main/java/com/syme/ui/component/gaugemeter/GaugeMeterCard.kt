package com.syme.ui.component.gaugemeter

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GaugeMeterCard(
    title: String,
    value: Float,
    unit: String,
    min: Float = 0f,
    max: Float = 100f,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Le GaugeMeter prend toute la largeur possible mais garde un ratio 1:1
            GaugeMeter(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                value = value,
                min = min,
                max = max
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Le card de la valeur prend toute la largeur disponible
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp) // ou toute hauteur souhaitée
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(), // prend toute la surface du Card
                    contentAlignment = Alignment.Center // centre le contenu verticalement et horizontalement
                ) {
                    Text(
                        text = "$title: ${value.toInt()} $unit",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GaugeMeterCardPreview() {
    GaugeMeterCard(
        title = "Température",
        value = 60f,
        unit = "°C",
        min = 0f,
        max = 120f,
        modifier = Modifier.padding(16.dp)
    )
}
