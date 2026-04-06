package com.syme.ui.screen.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.syme.ui.navigation.main.MainRoute
import com.syme.ui.navigation.main.mainNavGraph
import com.syme.ui.screen.home.BotFab
import com.syme.ui.screen.home.components.HomeBottomBar
import com.syme.ui.screen.home.HomeHeader
import com.syme.ui.snapshot.GlobalMessageSnapshot
import com.syme.ui.viewmodel.ApplianceViewModel
import com.syme.ui.viewmodel.BillViewModel
import com.syme.ui.viewmodel.BotViewModel
import com.syme.ui.viewmodel.CircuitViewModel
import com.syme.ui.viewmodel.ConsumptionViewModel
import com.syme.ui.viewmodel.InstallationViewModel
import com.syme.ui.viewmodel.MeterViewModel
import com.syme.ui.viewmodel.UserViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    userViewModel: UserViewModel,
    installationViewModel: InstallationViewModel,
    consumptionViewModel: ConsumptionViewModel,
    meterViewModel: MeterViewModel,
    applianceViewModel: ApplianceViewModel,
    circuitViewModel: CircuitViewModel,
    billViewModel: BillViewModel,
    botViewModel: BotViewModel
    ) {

    val mainNavController = rememberNavController()

    val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val routesWithoutTopBar = setOf(
        MainRoute.NotificationScreen.route,
        MainRoute.ProfileScreen.route,
        MainRoute.BotScreen.route,
    )

    val routesWithoutBottomBar = emptySet<String>()

    val routesWithoutFab = setOf(
        MainRoute.BotScreen.route
    )

    Scaffold(
        topBar = {
            if (currentRoute !in routesWithoutTopBar) {
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
            if (currentRoute !in routesWithoutBottomBar) {
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
        },
        floatingActionButton = {
            if (currentRoute !in routesWithoutFab) {
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
                    billViewModel = billViewModel,
                    botViewModel = botViewModel
                )
            }

            // 🔹 Snapshot global pour messages CRUD
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                GlobalMessageSnapshot(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 120.dp)
                        .widthIn(max = 620.dp)
                )
            }
        }
    }
}
