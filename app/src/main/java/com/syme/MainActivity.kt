package com.syme

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.material3.Scaffold
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.syme.ui.theme.SYMETheme
import com.syme.ui.viewmodel.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val splashScreenViewModel : SplashViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔹 Splash screen
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                splashScreenViewModel.isSplashScreenVisible.value
            }
        }

        enableEdgeToEdge()
        setContent {
            SYMETheme {
                Scaffold { paddingValues ->
                    App(paddingValues = paddingValues)
                }
            }
        }
    }
}
