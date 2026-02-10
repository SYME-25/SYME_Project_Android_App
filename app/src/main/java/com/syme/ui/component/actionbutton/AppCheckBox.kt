package com.syme.ui.component.actionbutton

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
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
fun AppCheckbox(
    checked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {},
    label: String = "",
    modifier: Modifier = Modifier,
    checkedColor: Color = MaterialTheme.colorScheme.primary,
    uncheckedColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = checkedColor,
                uncheckedColor = uncheckedColor,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        if (label.isNotEmpty()) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// ================== PREVIEWS ==================
@Preview(showBackground = true, name = "Unchecked Checkbox")
@Composable
fun PreviewUncheckedAppCheckbox() {
    var checked by remember { mutableStateOf(false) }

    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AppCheckbox(
                checked = checked,
                onCheckedChange = { checked = it },
                label = "Accept Terms & Conditions"
            )
        }
    }
}

@Preview(showBackground = true, name = "Checked Checkbox")
@Composable
fun PreviewCheckedAppCheckbox() {
    var checked by remember { mutableStateOf(true) }

    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AppCheckbox(
                checked = checked,
                onCheckedChange = { checked = it },
                label = "Subscribe to newsletter"
            )
        }
    }
}

@Preview(showBackground = true, name = "Custom Color Checkbox")
@Composable
fun PreviewCustomColorAppCheckbox() {
    var checked by remember { mutableStateOf(true) }

    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AppCheckbox(
                checked = checked,
                onCheckedChange = { checked = it },
                label = "Custom Colors",
                checkedColor = Color.Green,
                uncheckedColor = Color.Red.copy(alpha = 0.5f)
            )
        }
    }
}

@Preview(showBackground = true, name = "Long Label Checkbox")
@Composable
fun PreviewLongLabelAppCheckbox() {
    var checked by remember { mutableStateOf(false) }

    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AppCheckbox(
                checked = checked,
                onCheckedChange = { checked = it },
                label = "I agree to share anonymized usage data to improve future updates of the app."
            )
        }
    }
}
