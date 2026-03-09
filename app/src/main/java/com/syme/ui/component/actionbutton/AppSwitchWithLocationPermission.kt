package com.syme.ui.component.actionbutton

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.syme.domain.model.Location
import com.syme.utils.hasLocationPermission

@Composable
fun AppSwitchWithLocationPermission(
    label: String,
    onLocationStateChanged: (Boolean, Location?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    var isChecked by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Fonction pour récupérer la localisation
    fun fetchLocation() {
        try {
            val cancellationTokenSource = CancellationTokenSource()

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    Log.d("LocationSwitch", "Location: ${location.latitude}, ${location.longitude}")
                    onLocationStateChanged(
                        true,
                        Location(
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                    )
                } else {
                    Log.w("LocationSwitch", "Location is null, trying last location")
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                        if (lastLoc != null) {
                            onLocationStateChanged(
                                true,
                                Location(
                                    latitude = lastLoc.latitude,
                                    longitude = lastLoc.longitude
                                )
                            )
                        } else {
                            Log.e("LocationSwitch", "No location available")
                            onLocationStateChanged(true, Location())
                        }
                    }
                }
            }.addOnFailureListener { e ->
                Log.e("LocationSwitch", "Failed to get location", e)
                onLocationStateChanged(true, Location())
            }
        } catch (e: SecurityException) {
            Log.e("LocationSwitch", "Security exception", e)
            isChecked = false
            onLocationStateChanged(false, null)
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("LocationSwitch", "Permission granted: $isGranted")
        if (isGranted) {
            isChecked = true
            fetchLocation()
        } else {
            // Permission refusée
            isChecked = false
            onLocationStateChanged(false, null)

            // Vérifier si l'utilisateur a coché "Ne plus demander"
            val activity = context as? androidx.activity.ComponentActivity
            if (activity != null) {
                val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )

                if (!shouldShowRationale) {
                    // L'utilisateur a refusé définitivement
                    Log.d("LocationSwitch", "Permission permanently denied, showing settings dialog")
                    showSettingsDialog = true
                }
            }
        }
    }

    // Dialog pour rediriger vers les paramètres
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Permission de localisation requise") },
            text = {
                Text("Pour utiliser votre localisation, veuillez activer la permission dans les paramètres de l'application.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSettingsDialog = false
                        // Ouvrir les paramètres de l'app
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text("Ouvrir les paramètres")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    AppSwitch(
        checked = isChecked,
        onCheckedChange = { newChecked ->
            Log.d("LocationSwitch", "Switch toggled to: $newChecked")
            if (newChecked) {
                if (hasLocationPermission(context)) {
                    Log.d("LocationSwitch", "Permission already granted")
                    isChecked = true
                    fetchLocation()
                } else {
                    Log.d("LocationSwitch", "Requesting permission")
                    locationPermissionLauncher.launch(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                }
            } else {
                isChecked = false
                onLocationStateChanged(false, null)
            }
        },
        label = label,
        modifier = modifier
    )
}