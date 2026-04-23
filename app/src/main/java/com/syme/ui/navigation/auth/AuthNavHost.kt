package com.syme.ui.navigation.auth

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.syme.ui.screen.auth.AuthScreen
import com.syme.ui.viewmodel.LoginViewModel
import com.syme.ui.viewmodel.RegisterViewModel
import com.syme.ui.viewmodel.ResetPasswordViewModel
import com.syme.ui.viewmodel.SettingsViewModel

@Composable
fun AuthNavHost(navController: NavHostController) {
    val registerViewModel: RegisterViewModel      = hiltViewModel()
    val loginViewModel: LoginViewModel            = hiltViewModel()
    val resetPasswordViewModel: ResetPasswordViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()

    AuthScreen { contentPadding ->
        NavHost(
            navController    = navController,
            startDestination = AuthRoute.Login.route
        ) {
            authNavGraph(
                navController          = navController,
                registerViewModel      = registerViewModel,
                resetPasswordViewModel = resetPasswordViewModel,
                loginViewModel         = loginViewModel,
                settingsViewModel      = settingsViewModel,
                contentPadding         = contentPadding
            )
        }
    }
}