package com.syme

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.syme.data.preferences.UserPreferencesRepository
import com.syme.domain.model.enumeration.AppTheme
import com.syme.ui.navigation.RootNavGraph
import com.syme.ui.theme.SYMETheme
import com.syme.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository
    // SettingsViewModel est scoped à l'Activity — il survit aux recompositions
    private val settingsViewModel: SettingsViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("FCM_TOKEN", "Permission notifications accordée")
        } else {
            Log.w("FCM_TOKEN", "Permission notifications refusée !")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askNotificationPermission()
        installSplashScreen()
        enableEdgeToEdge()

        setContent {
            val language by settingsViewModel.language.collectAsStateWithLifecycle()
            val theme by settingsViewModel.theme.collectAsStateWithLifecycle()
            val systemInDarkTheme = isSystemInDarkTheme()

            // ✅ Applique la locale DANS le thread principal, de façon synchrone
            // key(language) force la destruction/recréation du sous-arbre Compose
            val locale = java.util.Locale(language.tag)
            val config = resources.configuration
            config.setLocale(locale)
            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)

            val isDark = when (theme) {
                AppTheme.DARK   -> true
                AppTheme.LIGHT  -> false
                AppTheme.SYSTEM -> systemInDarkTheme
            }

            val navController = rememberNavController()

            // ✅ key(language) : quand la langue change, tout le sous-arbre
            //    est recréé → les stringResource() sont relus dans la bonne locale
            key(language) {
                SYMETheme(darkTheme = isDark) {
                    RootNavGraph(navController = navController)
                }
            }
        }
    }
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("FCM", "Permission déjà accordée")
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}
