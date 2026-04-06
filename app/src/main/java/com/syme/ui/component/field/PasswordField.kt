package com.syme.ui.component.field

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
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
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isError = error.isNotEmpty()
    var isFocused by remember { mutableStateOf(false) }
    val colors = rememberFieldColors(isError = isError, isFocused = isFocused)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 20.dp)
            .fieldBase(colors, borderWidth = if (isError) 1.5.dp else 1.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Lock,
            contentDescription = null,
            tint = colors.icon,
            modifier = Modifier.size(22.dp)
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { isFocused = it.isFocused },
            textStyle = LocalTextStyle.current.copy(color = colors.text),
            cursorBrush = SolidColor(colors.cursor),
            visualTransformation = if (passwordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            decorationBox = { innerTextField ->
                FieldDecorationBox(
                    value = value,
                    isFocused = isFocused,
                    label = if (isError) error else label,
                    labelColor = colors.label,
                    innerTextField = innerTextField
                )
            }
        )
        Icon(
            painter = if (passwordVisible)
                painterResource(R.drawable.baseline_visibility_24)
            else
                painterResource(R.drawable.baseline_visibility_off_24),
            contentDescription = null,
            tint = colors.icon,
            modifier = Modifier
                .size(22.dp)
                .clickable { onToggleVisibility() }
        )
    }
}