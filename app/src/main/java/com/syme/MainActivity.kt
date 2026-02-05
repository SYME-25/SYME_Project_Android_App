package com.syme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Scaffold
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.syme.ui.theme.SYMETheme
import com.syme.ui.viewmodel.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val splashScreenViewModel : SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        //Pour la splash screen
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                splashScreenViewModel.isSplashScreenVisible.value
            }
        }

        super.onCreate(savedInstanceState)
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
