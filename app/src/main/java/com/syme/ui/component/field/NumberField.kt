package com.syme.ui.component.field

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String
) {
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 20.dp),
        value = value,
        onValueChange = { newValue ->
            // Autorise chiffres + une seule virgule ou un seul point
            val filtered = newValue
                .replace(',', '.')   // unifie en point
                .let { text ->
                    if (text.count { it == '.' } <= 1) text else value
                }

            if (filtered.all { it.isDigit() || it == '.' }) {
                onValueChange(filtered)
            }
        },
        label = {
            Text(
                text = error.ifEmpty { label },
                color = if (error.isNotEmpty()) Color.Red else Color.Unspecified
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
        ),
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}
