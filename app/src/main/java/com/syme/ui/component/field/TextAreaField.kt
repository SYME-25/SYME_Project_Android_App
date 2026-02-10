package com.syme.ui.component.field

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.ImeAction

@Composable
fun TextAreaField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String = ""
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 20.dp),
        label = {
            Text(
                text = if (error.isEmpty()) label else error,
                color = if (error.isEmpty()) Color.Unspecified else Color.Red
            )
        },
        maxLines = 6,  // multi-ligne
        minLines = 3,  // hauteur initiale
        singleLine = false,
        shape = RoundedCornerShape(8.dp),
        isError = error.isNotEmpty(),
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Default
        ),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Red,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}
