package com.syme.ui.component.field

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun NameField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String,
    icon: @Composable (() -> Unit)? = {
        Icon(Icons.Rounded.Person, contentDescription = "")
    }
) {
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 20.dp),
        value = value,
        onValueChange = { newValue ->
            if (newValue.all { it.isLetter() || it.isWhitespace() }) {
                onValueChange(newValue)
            }
        },
        label = {
            Text(
                text = error.ifEmpty { label },
                color = if (error.isNotEmpty()) Color.Red else Color.Unspecified
            )
        },
        leadingIcon = {
            icon?.invoke()
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text
        ),
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}
