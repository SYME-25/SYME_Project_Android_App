package com.syme.ui.component.dialog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.syme.R
import com.syme.domain.model.Appliance
import com.syme.ui.component.actionbutton.AppButton
import com.syme.ui.component.actionbutton.AppTextButton

// ─────────────────────────────────────────────────────────────────────────────
// DELETE
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ApplianceDeleteDialog(
    appliance: Appliance,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Outlined.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = stringResource(R.string.dialog_delete_appliance_title),
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = stringResource(
                    R.string.dialog_delete_appliance_message,
                    appliance.name.ifBlank { appliance.applianceId }
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            AppButton(
                text = stringResource(R.string.action_delete),
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            )
        },
        dismissButton = {
            AppTextButton(
                text = stringResource(R.string.action_cancel),
                onClick = onDismiss,
                textColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}
