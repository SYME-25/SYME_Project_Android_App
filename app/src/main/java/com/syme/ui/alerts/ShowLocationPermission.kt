package com.syme.ui.alerts

/**
 * Affiche une alerte demandant la permission d'utiliser la localisation.
 *
 * @param onConfirmed Callback si l'utilisateur accepte
 * @param onCancelled Callback si l'utilisateur refuse
 */
fun showLocationPermissionDialog(
    onConfirmed: () -> Unit,
    onCancelled: (() -> Unit)? = null
) {
    AlertManager.showAlert(
        title = "Location Access Required",
        message = "This feature needs access to your device location. Do you want to allow it?",
        confirmText = "Allow",
        cancelText = "Deny",
        onConfirm = onConfirmed,
        onCancel = onCancelled
    )
}
