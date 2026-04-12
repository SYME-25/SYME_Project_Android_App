package com.syme.ui.screen.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    val routesWithoutFab = setOf(MainRoute.BotScreen.route)

    val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomBarTotalHeight = 72.dp + 24.dp + navBarHeight

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent,
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
        bottomBar = {},
    ) { scaffoldPadding ->
        Box(modifier = Modifier.fillMaxSize()) {

            NavHost(
                navController = mainNavController,
                startDestination = MainRoute.HomeScreen.route,
                modifier = Modifier
                    .padding(scaffoldPadding)
                    .padding(bottom = bottomBarTotalHeight)
            ) {
                mainNavGraph(
                    navController = mainNavController,
                    paddingValues = scaffoldPadding,
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

            // ── Bottom Bar ────────────────────────────────────────────────────
            if (currentRoute !in routesWithoutBottomBar) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = navBarHeight),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    HomeBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { route ->

                            // Si on est sur BotScreen, on retire Bot de la stack avant de naviguer
                            if (currentRoute == MainRoute.BotScreen.route) {
                                mainNavController.popBackStack()
                            }

                            mainNavController.navigate(route) {
                                popUpTo(MainRoute.HomeScreen.route) {
                                    saveState = true
                                    inclusive = false
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }

            // ── FAB ───────────────────────────────────────────────────────────
            if (currentRoute !in routesWithoutFab) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 16.dp, bottom = bottomBarTotalHeight + 16.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    BotFab {
                        mainNavController.navigate(MainRoute.BotScreen.route) {
                            launchSingleTop = true
                        }
                    }
                }
            }

            // ── Snapshot ──────────────────────────────────────────────────────
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                GlobalMessageSnapshot(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = bottomBarTotalHeight + 8.dp)
                        .widthIn(max = 620.dp)
                )
            }
        }
    }
}