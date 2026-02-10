package com.syme.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.syme.domain.model.Location

fun hasLocationPermission(context: Context): Boolean {
    return androidx.core.content.ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

// Récupère la dernière localisation
@SuppressLint("MissingPermission")
fun fetchLastLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Location) -> Unit
) {
    fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
        if (loc != null) {
            onLocationReceived(
                Location(
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    accuracy_m = loc.accuracy,
                    lastUpdatedEpochMs = System.currentTimeMillis()
                )
            )
        }
    }
}
