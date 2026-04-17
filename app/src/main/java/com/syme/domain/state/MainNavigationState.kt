package com.syme.domain.state

import androidx.navigation.NavHostController
import com.syme.ui.navigation.main.MainRoute

class MainNavigationState {
    fun handleBack(
        route: String?,
        navController: NavHostController
    ): Boolean {
        return when (route) {
            MainRoute.BotScreen.route,
            MainRoute.NotificationScreen.route,
            MainRoute.ProfileScreen.route -> {
                navController.popBackStack()
                true
            }
            else -> false
        }
    }
}