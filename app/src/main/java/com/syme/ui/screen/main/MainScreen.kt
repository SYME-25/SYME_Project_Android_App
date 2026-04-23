package com.syme.ui.screen.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import dev.chrisbanes.haze.HazeState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.syme.ui.navigation.main.MainRoute
import com.syme.ui.screen.bot.components.BotTopBar
import com.syme.ui.screen.home.components.BotFab
import com.syme.ui.screen.home.components.HomeBottomBar
import com.syme.ui.screen.home.HomeHeader
import com.syme.ui.snapshot.GlobalMessageSnapshot
import com.syme.ui.viewmodel.BotViewModel
import com.syme.ui.viewmodel.NotificationsViewModel
import dev.chrisbanes.haze.hazeSource

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    navController: NavHostController,
    notificationsViewModel: NotificationsViewModel,
    botViewModel: BotViewModel,
    content: @Composable (PaddingValues) -> Unit
) {
    val unreadCount by notificationsViewModel.unreadCount
        .collectAsStateWithLifecycle()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val routesWithoutTopBar = setOf(
        MainRoute.NotificationScreen.route,
        MainRoute.ProfileScreen.route,
        MainRoute.BotScreen.route,
    )

    val routesWithoutFab = setOf(
        MainRoute.BotScreen.route,
        MainRoute.NotificationScreen.route,
        MainRoute.ProfileScreen.route,
        MainRoute.InstallationDetailScreen.route,
        MainRoute.UserInstallationDetailScreen.route,
        MainRoute.ApplianceDetailScreen.route
    )

    val routesWithoutBottomBar = setOf(
        MainRoute.NotificationScreen.route,
        MainRoute.ProfileScreen.route,
        MainRoute.BotScreen.route,
        MainRoute.InstallationDetailScreen.route,
        MainRoute.UserInstallationDetailScreen.route,
        MainRoute.ApplianceDetailScreen.route
    )

    var bottomBarHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val hazeState = remember { HazeState() }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0),

        topBar = {
            when (currentRoute) {
                MainRoute.BotScreen.route -> {
                    BotTopBar(
                        onOpenHistory = { botViewModel.onToggleDrawer() },
                        onNewConversation = { botViewModel.newConversation() },
                    )
                }
                !in routesWithoutTopBar -> {
                    HomeHeader(
                        onNotificationsClick = {
                            navController.navigateOverlay(MainRoute.NotificationScreen.route)
                        },
                        onProfileClick = {
                            navController.navigateOverlay(MainRoute.ProfileScreen.route)
                        },
                        unreadCount = unreadCount
                    )
                }
            }
        },

        bottomBar = {}
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            val bottomBarHeightDp = with(density) { bottomBarHeightPx.toDp() }

            val contentPadding = PaddingValues(
                bottom = if (currentRoute !in routesWithoutBottomBar)
                    bottomBarHeightDp + navBarHeight
                else
                    0.dp
            )

            // ── CONTENU ─────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(hazeState)
            ) {
                content(contentPadding)
            }

            // ── BOTTOM BAR ─────────────────────────
            if (currentRoute !in routesWithoutBottomBar) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = navBarHeight),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    HomeBottomBar(
                        hazeState = hazeState,
                        modifier = Modifier.onGloballyPositioned { coords ->
                            bottomBarHeightPx = coords.size.height
                        },
                        currentRoute = currentRoute,
                        onNavigate = { route ->

                            val tabRoutes = setOf(
                                MainRoute.HomeScreen.route,
                                MainRoute.ConsumptionScreen.route,
                                MainRoute.BillScreen.route,
                                MainRoute.SettingsScreen.route
                            )

                            val overlayRoutes = setOf(
                                MainRoute.BotScreen.route,
                                MainRoute.NotificationScreen.route,
                                MainRoute.ProfileScreen.route,
                            )

                            if (route in tabRoutes) {

                                val baseRoute = if (currentRoute in overlayRoutes) {
                                    navController.previousBackStackEntry?.destination?.route
                                        ?: MainRoute.HomeScreen.route
                                } else {
                                    currentRoute ?: MainRoute.HomeScreen.route
                                }

                                if (route != baseRoute) {

                                    // Nettoyer overlay éventuel
                                    while (navController.currentBackStackEntry?.destination?.route in overlayRoutes) {
                                        navController.popBackStack()
                                    }

                                    navController.navigate(route) {
                                        popUpTo(MainRoute.HomeScreen.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                } else {
                                    // Juste fermer overlay si présent
                                    while (navController.currentBackStackEntry?.destination?.route in overlayRoutes) {
                                        navController.popBackStack()
                                    }
                                }

                            } else {
                                navController.navigateOverlay(route)
                            }
                        }
                    )
                }
            }

            // ── FAB ────────────────────────────────
            if (currentRoute !in routesWithoutFab) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            bottom = navBarHeight + with(density) { bottomBarHeightPx.toDp() } + 12.dp,
                            end = 16.dp
                        ),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    BotFab(
                        onClick = { navController.navigateOverlay(MainRoute.BotScreen.route) },
                        hazeState = hazeState
                    )
                }
            }

            // ── SNAPSHOT ───────────────────────────
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                GlobalMessageSnapshot(
                    hazeState = hazeState,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 80.dp)
                        .widthIn(max = 620.dp),
                    paddingValues = contentPadding
                )
            }
        }
    }
}

// ── NAVIGATION OVERLAY CLEAN ─────────────────────
fun NavHostController.navigateOverlay(route: String) {
    navigate(route) {
        popUpTo(route) { inclusive = true }
        launchSingleTop = true
    }
}