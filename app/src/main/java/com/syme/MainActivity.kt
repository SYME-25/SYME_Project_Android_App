package com.syme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.syme.ui.theme.SYMETheme
import com.syme.viewmodel.SplashScreenViewModel

class MainActivity : ComponentActivity() {

    private val splashScreenViewModel : SplashScreenViewModel by viewModels()

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
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    App(paddingValues = innerPadding)
                }
            }
        }
    }
}
