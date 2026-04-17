package com.syme.ui.navigation.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.syme.ui.screen.main.MainScreen
import com.syme.ui.viewmodel.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val installationViewModel: InstallationViewModel = hiltViewModel()
    val consumptionViewModel: ConsumptionViewModel = hiltViewModel()
    val applianceViewModel: ApplianceViewModel = hiltViewModel()
    val meterViewModel: MeterViewModel = hiltViewModel()
    val circuitViewModel: CircuitViewModel = hiltViewModel()
    val billViewModel: BillViewModel = hiltViewModel()
    val userViewModel: UserViewModel = hiltViewModel()
    val botViewModel: BotViewModel = hiltViewModel()
    val notificationViewModel: NotificationsViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()

    MainScreen(
        navController = navController,
        notificationsViewModel = notificationViewModel,
        botViewModel = botViewModel
    ) { contentPadding ->
        NavHost(
                navController = navController,
                startDestination = MainRoute.HomeScreen.route
            ) {
                mainNavGraph(
                    navController = navController,
                    authViewModel = authViewModel,
                    userViewModel = userViewModel,
                    installationViewModel = installationViewModel,
                    consumptionViewModel = consumptionViewModel,
                    meterViewModel = meterViewModel,
                    applianceViewModel = applianceViewModel,
                    circuitViewModel = circuitViewModel,
                    billViewModel = billViewModel,
                    botViewModel = botViewModel,
                    notificationsViewModel = notificationViewModel,
                    settingsViewModel = settingsViewModel,
                    contentPadding = contentPadding
                )
            }
    }
}