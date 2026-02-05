package com.syme.ui.alerts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syme.ui.component.actionbutton.AppButton

/**
 * Composable qui affiche toutes les alertes/dialogues actives.
 * On ne montre qu'une seule alerte à la fois.
 */
@Composable
fun AlertHost() {
    val alerts by AlertManager.alerts.collectAsState()

    alerts.firstOrNull()?.let { alert ->
        AlertDialog(
            onDismissRequest = { AlertManager.dismissAlert(alert) },
            title = { Text(alert.title) },
            text = { Text(alert.message) },
            confirmButton = {
                // Si cancelText est présent, on affiche les deux boutons côte à côte
                if (alert.cancelText != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppButton(
                            text = alert.cancelText,
                            onClick = {
                                alert.onCancel?.invoke()
                                AlertManager.dismissAlert(alert)
                            },
                            modifier = Modifier.weight(1f),
                        )
                        AppButton(
                            text = alert.confirmText,
                            onClick = {
                                alert.onConfirm?.invoke()
                                AlertManager.dismissAlert(alert)
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    // Sinon on ne montre que le bouton de confirmation
                    AppButton(
                        text = alert.confirmText,
                        onClick = {
                            alert.onConfirm?.invoke()
                            AlertManager.dismissAlert(alert)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            dismissButton = {} // vide car on gère cancel dans Row
        )
    }
}
