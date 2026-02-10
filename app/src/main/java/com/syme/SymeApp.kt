package com.syme

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.syme.ui.snapshot.appContext
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SymeApp : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        // ✅ init du context global
        appContext = applicationContext
    }
}

