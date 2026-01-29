package com.syme.ui.component.field

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.syme.R

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String,
    passwordVisible: Boolean,
    onToggleVisibility: () -> Unit
) {
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 20.dp),
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = error.ifEmpty { label },
                color = if (error.isNotEmpty()) Color.Red else Color.Unspecified
            )
        },
        leadingIcon = {
            Icon(Icons.Rounded.Lock, contentDescription = "")
        },
        visualTransformation =
            if (passwordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
        trailingIcon = {
            val image =
                if (passwordVisible) painterResource(R.drawable.baseline_visibility_24)
                else painterResource(R.drawable.baseline_visibility_off_24)

            Icon(
                painter = image,
                contentDescription = "",
                modifier = Modifier.clickable { onToggleVisibility() }
            )
        },
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}
