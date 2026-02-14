package com.syme.ui.screen.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.syme.ui.navigation.main.MainRoute
import com.syme.ui.navigation.main.mainNavGraph
import com.syme.ui.screen.home.HomeBottomBar
import com.syme.ui.screen.home.HomeHeader
import com.syme.ui.snapshot.MessageType
import com.syme.ui.snapshot.globalMessageManager
import com.syme.ui.viewmodel.ApplianceViewModel
import com.syme.ui.viewmodel.CircuitViewModel
import com.syme.ui.viewmodel.ConsumptionViewModel
import com.syme.ui.viewmodel.InstallationViewModel
import com.syme.ui.viewmodel.MeasurementViewModel
import com.syme.ui.viewmodel.MeterViewModel
import com.syme.utils.connectivityFlow
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    installationViewModel: InstallationViewModel,
    consumptionViewModel: ConsumptionViewModel,
    measurementViewModel: MeasurementViewModel,
    meterViewModel: MeterViewModel,
    applianceViewModel: ApplianceViewModel,
    circuitViewModel: CircuitViewModel
    ) {

    val mainNavController = rememberNavController()

    val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = { HomeHeader() },
        bottomBar = {
            HomeBottomBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    mainNavController.navigate(route) {
                        popUpTo(MainRoute.HomeScreen.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { padding ->
        NavHost(
            navController = mainNavController,
            startDestination = MainRoute.HomeScreen.route,
            modifier = Modifier.padding(padding)
        ) {
            mainNavGraph(
                navController = mainNavController,
                paddingValues = padding,
                installationViewModel = installationViewModel,
                consumptionViewModel = consumptionViewModel,
                measurementViewModel = measurementViewModel,
                meterViewModel = meterViewModel,
                applianceViewModel = applianceViewModel,
                circuitViewModel = circuitViewModel
            )
        }
    }
}
