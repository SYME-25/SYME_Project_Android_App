package com.syme.ui.screen.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.syme.ui.navigation.main.MainRoute
import com.syme.ui.navigation.main.mainNavGraph
import com.syme.ui.screen.home.BotFab
import com.syme.ui.screen.home.HomeBottomBar
import com.syme.ui.screen.home.HomeHeader
import com.syme.ui.snapshot.GlobalMessageSnapshot
import com.syme.ui.snapshot.MessageType
import com.syme.ui.snapshot.globalMessageManager
import com.syme.ui.viewmodel.ApplianceViewModel
import com.syme.ui.viewmodel.BillViewModel
import com.syme.ui.viewmodel.CircuitViewModel
import com.syme.ui.viewmodel.ConsumptionViewModel
import com.syme.ui.viewmodel.InstallationViewModel
import com.syme.ui.viewmodel.MeterViewModel
import com.syme.ui.viewmodel.UserViewModel
import com.syme.utils.connectivityFlow
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    userViewModel: UserViewModel,
    installationViewModel: InstallationViewModel,
    consumptionViewModel: ConsumptionViewModel,
    meterViewModel: MeterViewModel,
    applianceViewModel: ApplianceViewModel,
    circuitViewModel: CircuitViewModel,
    billViewModel: BillViewModel
    ) {

    val mainNavController = rememberNavController()

    val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            if (currentRoute != MainRoute.NotificationScreen.route && currentRoute != MainRoute.ProfileScreen.route) {
                HomeHeader(
                    onNotificationsClick = {
                        mainNavController.navigate(MainRoute.NotificationScreen.route) {
                            launchSingleTop = true
                        }
                    },
                    onProfileClick = {
                        mainNavController.navigate(MainRoute.ProfileScreen.route) {
                            launchSingleTop = true
                        }
                    },
                    unreadCount = 5
                )
            }
        },
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
        },
        floatingActionButton = {
            if (currentRoute != MainRoute.BotScreen.route) {
                BotFab {
                    mainNavController.navigate(MainRoute.BotScreen.route) {
                        launchSingleTop = true
                    }
                }
            }
        }
    ) { padding ->

        Box {
            NavHost(
                navController = mainNavController,
                startDestination = MainRoute.HomeScreen.route,
                modifier = Modifier.padding(padding)
            ) {
                mainNavGraph(
                    navController = mainNavController,
                    paddingValues = padding,
                    userViewModel = userViewModel,
                    installationViewModel = installationViewModel,
                    consumptionViewModel = consumptionViewModel,
                    meterViewModel = meterViewModel,
                    applianceViewModel = applianceViewModel,
                    circuitViewModel = circuitViewModel,
                    billViewModel = billViewModel
                )
            }

            // 🔹 Snapshot global pour messages CRUD
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                GlobalMessageSnapshot(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 100.dp)
                        .widthIn(max = 620.dp)
                )
            }
        }
    }
}
