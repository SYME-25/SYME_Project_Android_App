package com.syme

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import com.google.firebase.FirebaseApp
import com.syme.ui.snapshot.appContext
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SymeApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        FirebaseApp.initializeApp(this)

        // ✅ init du context global
        appContext = applicationContext
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "syme_notifications",   // ✅ doit matcher channelId dans le service
                "SYME Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications liées à l'énergie et aux alertes"
                enableVibration(true)
                enableLights(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}

