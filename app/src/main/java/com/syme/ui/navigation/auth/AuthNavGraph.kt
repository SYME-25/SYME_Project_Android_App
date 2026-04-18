package com.syme.ui.navigation.auth

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import com.syme.ui.screen.auth.LoginScreen
import com.syme.ui.screen.auth.PrivacyPolicyScreen
import com.syme.ui.screen.auth.RegisterScreen
import com.syme.ui.screen.auth.ResetPasswordStep1Screen
import com.syme.ui.screen.auth.ResetPasswordStep2Screen
import com.syme.ui.viewmodel.LoginViewModel
import com.syme.ui.viewmodel.RegisterViewModel
import com.syme.ui.viewmodel.ResetPasswordViewModel

fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
    registerViewModel: RegisterViewModel,
    resetPasswordViewModel: ResetPasswordViewModel,
    loginViewModel: LoginViewModel,
    contentPadding: PaddingValues
) {

    composable(AuthRoute.Login.route) {
        LoginScreen(
            viewModel = loginViewModel,
            onNavigateToRegister = { navController.navigate(AuthRoute.Register.route) },
            onNavigateToResetPassword = { navController.navigate(AuthRoute.ResetPasswordStep1.route) },
            contentPadding = contentPadding
        )
    }

    composable(AuthRoute.Register.route) {
        RegisterScreen(
            viewModel = registerViewModel,
            navController = navController,
            onNavigateBack = { navController.popBackStack() },
            contentPadding = contentPadding
        )
    }

    composable(AuthRoute.ResetPasswordStep1.route) {
        ResetPasswordStep1Screen(
            navController = navController,
            onBackToLogin = { navController.popBackStack() },
            viewModel = resetPasswordViewModel,
            contentPadding = contentPadding
        )
    }

    composable(AuthRoute.ResetPasswordStep2.route) {
        ResetPasswordStep2Screen(
            navController = navController,
            onResetComplete = { navController.popBackStack(AuthRoute.Login.route, false) },
            contentPadding = contentPadding
        )
    }

    composable(AuthRoute.PrivacyPolicy.route) {
        PrivacyPolicyScreen()
    }
}