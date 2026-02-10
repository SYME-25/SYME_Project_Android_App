package com.syme.ui.component.actionbutton

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import com.syme.domain.model.Location
import com.syme.ui.alerts.showLocationPermissionDialog
import com.syme.utils.fetchLastLocation
import com.syme.utils.hasLocationPermission

@Composable
fun AppSwitchWithLocationPermission(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    onLocationReceived: (Location) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchLastLocation(fusedLocationClient) { loc ->
                onLocationReceived(loc)
            }
            onCheckedChange(true)
        } else {
            onCheckedChange(false)
        }
    }

    AppSwitch(
        checked = checked,
        onCheckedChange = { newChecked ->
            if (newChecked) {
                showLocationPermissionDialog(
                    onConfirmed = {
                        if (hasLocationPermission(context)) {
                            fetchLastLocation(fusedLocationClient) { loc ->
                                onLocationReceived(loc)
                            }
                            onCheckedChange(true)
                        } else {
                            locationPermissionLauncher.launch(
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        }
                    },
                    onCancelled = {
                        onCheckedChange(false)
                    }
                )
            } else {
                onCheckedChange(false)
            }
        },
        label = label,
        modifier = modifier
    )
}