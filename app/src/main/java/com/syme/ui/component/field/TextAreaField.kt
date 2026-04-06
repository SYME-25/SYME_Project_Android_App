package com.syme.ui.component.field

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun TextAreaField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String = "",
    modifier: Modifier = Modifier
) {
    val isError = error.isNotEmpty()
    var isFocused by remember { mutableStateOf(false) }
    val colors = rememberFieldColors(isError = isError, isFocused = isFocused)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 20.dp)
            .fieldBase(colors, borderWidth = if (isError) 1.5.dp else 1.dp)
            .padding(horizontal = 16.dp)
            .onFocusChanged { isFocused = it.isFocused },
        textStyle = LocalTextStyle.current.copy(color = colors.text),
        maxLines = 6,
        minLines = 3,
        cursorBrush = SolidColor(colors.cursor),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Default),
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
}