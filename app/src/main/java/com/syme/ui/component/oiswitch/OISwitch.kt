package com.syme.ui.component.oiswitch

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OiSwitch(
    isOn: Boolean,                 // true = I, false = O
    onToggle: (Boolean) -> Unit,   // action déclenchée
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(64.dp)
            .height(120.dp)
            .shadow(4.dp, RoundedCornerShape(32.dp))
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(32.dp)
            )
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        // Bouton I (ON) -> VERT
        SwitchButton(
            text = "I",
            selected = isOn,
            selectedColor = Color(0xFF2ECC71), // vert
            onClick = { onToggle(true) },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Bouton O (OFF) -> ROUGE
        SwitchButton(
            text = "O",
            selected = !isOn,
            selectedColor = Color(0xFFE74C3C), // rouge
            onClick = { onToggle(false) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SwitchButton(
    text: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        tonalElevation = if (selected) 1.dp else 8.dp, // effet bouton enfoncé
        color = if (selected)
            selectedColor
        else
            MaterialTheme.colorScheme.surface
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected)
                    Color.White
                else
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OiSwitchPreview() {
    var isOn by remember { mutableStateOf(false) }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Interrupteur vertical O / I")

            Spacer(modifier = Modifier.height(16.dp))

            OiSwitch(
                isOn = isOn,
                onToggle = { newState ->
                    isOn = newState
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isOn) "État : I (ON)" else "État : O (OFF)",
                fontWeight = FontWeight.Bold
            )
        }
    }
}