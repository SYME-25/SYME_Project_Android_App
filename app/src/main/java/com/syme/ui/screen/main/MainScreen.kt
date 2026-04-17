package com.syme.ui.screen.main

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.syme.domain.state.MainNavigationState
import com.syme.ui.navigation.main.MainRoute
import com.syme.ui.screen.bot.components.BotTopBar
import com.syme.ui.screen.home.BotFab
import com.syme.ui.screen.home.components.HomeBottomBar
import com.syme.ui.screen.home.HomeHeader
import com.syme.ui.snapshot.GlobalMessageSnapshot
import com.syme.ui.viewmodel.BotViewModel
import com.syme.ui.viewmodel.NotificationsViewModel

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
        MainRoute.BotScreen.route
    )
    val routesWithoutBottomBar = emptySet<String>()

    var bottomBarHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val hazeState = remember { HazeState() }

    val navigationState = remember { MainNavigationState() }

    BackHandler {
        val handled = navigationState.handleBack(currentRoute, navController)

        if (!handled) {
            navController.popBackStack()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            if (currentRoute !in routesWithoutTopBar) {
                HomeHeader(
                    onNotificationsClick = {
                        navController.navigate(MainRoute.NotificationScreen.route)
                    },
                    onProfileClick = {
                        navController.navigate(MainRoute.ProfileScreen.route)
                    },
                    unreadCount = unreadCount
                )
            }
            if (currentRoute == MainRoute.BotScreen.route) {
                BotTopBar(
                    onOpenHistory = { botViewModel.onToggleDrawer() },
                    onNewConversation = { botViewModel.newConversation() }
                )
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

            // ── CONTENU (NavHost) ──────────────────────────────────
            Box(modifier = Modifier.fillMaxSize().haze(hazeState)) {
                content(contentPadding)
            }

            // ── BOTTOM BAR overlay ────────────────────────────────
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
                                // Résoudre le "tab de base" réel sous l'overlay éventuel
                                val baseRoute = if (currentRoute in overlayRoutes) {
                                    navController.previousBackStackEntry?.destination?.route
                                        ?: MainRoute.HomeScreen.route
                                } else {
                                    currentRoute ?: MainRoute.HomeScreen.route
                                }

                                if (route == baseRoute) {
                                    // ✅ On est déjà sur ce tab (ou on y revient depuis son overlay)
                                    // → on dépile juste l'overlay si besoin, sans navigate()
                                    if (currentRoute in overlayRoutes) {
                                        navController.popBackStack()
                                    }
                                    // Si déjà sur le bon tab sans overlay → rien à faire
                                } else {
                                    // ✅ Changement de tab réel
                                    // → on dépile l'overlay d'abord si nécessaire
                                    if (currentRoute in overlayRoutes) {
                                        navController.popBackStack()
                                    }
                                    navController.navigate(route) {
                                        popUpTo(MainRoute.HomeScreen.route) {
                                            saveState = true
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            } else {
                                // Navigation vers un overlay
                                navController.navigate(route) {
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
            }

            // ── FAB ───────────────────────────────────────────────
            if (currentRoute !in routesWithoutFab) {
                val bottomBarHeightDp = with(density) { bottomBarHeightPx.toDp() }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            bottom = navBarHeight + bottomBarHeightDp + 12.dp, // ✅ dynamique
                            end = 16.dp
                        ),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    BotFab {
                        navController.navigate(MainRoute.BotScreen.route)
                    }
                }
            }

            // ── SNAPSHOT ──────────────────────────────────────────
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                GlobalMessageSnapshot(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 80.dp) // ✅ au-dessus de la BottomBar
                        .widthIn(max = 620.dp)
                )
            }
        }
    }
}