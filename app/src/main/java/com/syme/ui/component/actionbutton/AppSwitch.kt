package com.syme.ui.component.actionbutton

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String = "",
    checkedColor: Color = MaterialTheme.colorScheme.primary,
    uncheckedColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
    modifier: Modifier = Modifier
) {
    val switchColors = SwitchDefaults.colors(
        checkedThumbColor = MaterialTheme.colorScheme.onSurface,
        checkedTrackColor = checkedColor.copy(alpha = 0.35f),
        checkedBorderColor = checkedColor,

        uncheckedThumbColor = uncheckedColor,
        uncheckedTrackColor = uncheckedColor.copy(alpha = 0.35f),
        uncheckedBorderColor = uncheckedColor,

        disabledCheckedThumbColor = checkedColor.copy(alpha = 0.4f),
        disabledCheckedTrackColor = checkedColor.copy(alpha = 0.2f),
        disabledUncheckedThumbColor = uncheckedColor.copy(alpha = 0.4f),
        disabledUncheckedTrackColor = uncheckedColor.copy(alpha = 0.2f)
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(vertical = 4.dp)
    ) {
        if (label.isNotEmpty()) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(16.dp))
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = switchColors
        )
    }
}

@Preview(showBackground = true, name = "Default Switch")
@Composable
fun PreviewDefaultAppSwitch() {
    var checked by remember { mutableStateOf(false) }

    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AppSwitch(
                checked = checked,
                onCheckedChange = { checked = it },
                label = "Enable Notifications"
            )
        }
    }
}

@Preview(showBackground = true, name = "Checked Switch")
@Composable
fun PreviewCheckedAppSwitch() {
    var checked by remember { mutableStateOf(true) }

    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AppSwitch(
                checked = checked,
                onCheckedChange = { checked = it },
                label = "Dark Mode"
            )
        }
    }
}

@Preview(showBackground = true, name = "Long Label Switch")
@Composable
fun PreviewLongLabelAppSwitch() {
    var checked by remember { mutableStateOf(false) }

    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AppSwitch(
                checked = checked,
                onCheckedChange = { checked = it },
                label = "Allow background data usage during low power mode"
            )
        }
    }
}