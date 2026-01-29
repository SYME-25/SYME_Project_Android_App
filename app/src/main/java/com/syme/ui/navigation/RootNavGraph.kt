package com.syme.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.syme.ui.navigation.auth.AuthRoute
import com.syme.ui.navigation.auth.authNavGraph
import com.syme.ui.navigation.main.MainRoute
import com.syme.ui.navigation.main.mainNavGraph
import com.syme.ui.screen.main.MainScreen


/**
 * RootNavGraph : centralise toutes les features de navigation
 *
 * Point d’entrée unique pour NavHost
 * - Auth
 * - Main (Home, Profile, etc.)
 */
@Composable
fun RootNavGraph(
    paddingValues: PaddingValues,
    navController: NavHostController,
    startDestination: String = RootRoute.Auth // on démarre par l'auth
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // --- NavGraph imbriqué pour Auth ---
        navigation(
            startDestination = AuthRoute.Login.route, // première screen de l’auth
            route = RootRoute.Auth //               // route parent
        ) {
            authNavGraph(navController, paddingValues)
        }

        composable(MainRoute.MainScreen.route) {
            MainScreen()
        }
    }
}
