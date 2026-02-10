package com.syme.ui.navigation.auth

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import com.syme.ui.screen.auth.LoginScreen
import com.syme.ui.screen.auth.RegisterScreen
import com.syme.ui.screen.auth.ResetPasswordStep1Screen
import com.syme.ui.screen.auth.ResetPasswordStep2Screen
import com.syme.ui.viewmodel.LoginViewModel
import com.syme.ui.viewmodel.RegisterViewModel

fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
    registerViewModel: RegisterViewModel,
    loginViewModel: LoginViewModel
) {

    composable(AuthRoute.Login.route) {
        LoginScreen(
            viewModel = loginViewModel,
            navController = navController,
            onNavigateToRegister = { navController.navigate(AuthRoute.Register.route) },
            onNavigateToResetPassword = { navController.navigate(AuthRoute.ResetPasswordStep1.route) }
        )
    }

    composable(AuthRoute.Register.route) {
        RegisterScreen(
            viewModel = registerViewModel,
            navController = navController,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(AuthRoute.ResetPasswordStep1.route) {
        ResetPasswordStep1Screen(
            navController = navController,
            onBackToLogin = { navController.popBackStack() },
            onNextStep = { navController.navigate(AuthRoute.ResetPasswordStep2.route) }
        )
    }

    composable(AuthRoute.ResetPasswordStep2.route) {
        ResetPasswordStep2Screen(
            navController = navController,
            onResetComplete = { navController.popBackStack(AuthRoute.Login.route, false) }
        )
    }
}