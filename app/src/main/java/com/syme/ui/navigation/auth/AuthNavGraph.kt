package com.syme.ui.navigation.auth

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import com.syme.ui.screen.auth.LoginScreen
import com.syme.ui.screen.auth.RegisterScreen
import com.syme.ui.screen.auth.ResetPasswordStep1Screen
import com.syme.ui.screen.auth.ResetPasswordStep2Screen

fun NavGraphBuilder.authNavGraph(navController: NavHostController, paddingValues: PaddingValues) {

    composable(AuthRoute.Login.route) {
        LoginScreen(
            navController = navController,
            paddingValues = paddingValues,
            onNavigateToRegister = { navController.navigate(AuthRoute.Register.route) },
            onNavigateToResetPassword = { navController.navigate(AuthRoute.ResetPasswordStep1.route) }
        )
    }

    composable(AuthRoute.Register.route) {
        RegisterScreen(
            navController = navController,
            paddingValues = paddingValues,
            onNavigateBack = { navController.popBackStack() },
            onRegistrationSuccess = {
                navController.navigate("main/home") {
                    popUpTo("auth") { inclusive = true } // supprime le stack auth
                }
            }
        )
    }

    composable(AuthRoute.ResetPasswordStep1.route) {
        ResetPasswordStep1Screen(
            navController = navController,
            paddingValues = paddingValues,
            onBackToLogin = { navController.popBackStack() },
            onNextStep = { navController.navigate(AuthRoute.ResetPasswordStep2.route) }
        )
    }

    composable(AuthRoute.ResetPasswordStep2.route) {
        ResetPasswordStep2Screen(
            navController = navController,
            paddingValues = paddingValues,
            onResetComplete = { navController.popBackStack(AuthRoute.Login.route, false) }
        )
    }
}