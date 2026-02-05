package com.syme.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.syme.data.session.SessionManager
import com.syme.ui.component.compositionlocal.LocalCurrentUserSession
import com.syme.ui.navigation.auth.AuthRoute
import com.syme.ui.navigation.auth.authNavGraph
import com.syme.ui.navigation.main.MainRoute
import com.syme.ui.navigation.main.mainNavGraph
import com.syme.ui.screen.main.MainScreen
import com.syme.ui.viewmodel.AuthViewModel
import com.syme.ui.viewmodel.ConsumptionViewModel
import com.syme.ui.viewmodel.InstallationViewModel
import com.syme.ui.viewmodel.LoginViewModel
import com.syme.ui.viewmodel.MeasurementViewModel
import com.syme.ui.viewmodel.RegisterViewModel


/**
 * RootNavGraph : centralise toutes les features de navigation
 *
 * Point d’entrée unique pour NavHost
 * - Auth
 * - Main (Home, Profile, etc.)
 */
@Composable
fun RootNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    val installationViewModel: InstallationViewModel = hiltViewModel()
    val registerViewModel: RegisterViewModel = hiltViewModel()
    val loginViewModel: LoginViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    val consumptionViewModel: ConsumptionViewModel = hiltViewModel()
    val measurementViewModel: MeasurementViewModel = hiltViewModel()
    val session by authViewModel.currentSession.collectAsState()

    CompositionLocalProvider(
        LocalCurrentUserSession provides session
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            navigation(
                startDestination = AuthRoute.Login.route,
                route = RootRoute.Auth
            ) {
                authNavGraph(navController, registerViewModel, loginViewModel)
            }

            composable(MainRoute.MainScreen.route) {
                MainScreen(
                    installationViewModel = installationViewModel,
                    consumptionViewModel = consumptionViewModel,
                    measurementViewModel = measurementViewModel
                )
            }
        }
    }
}
