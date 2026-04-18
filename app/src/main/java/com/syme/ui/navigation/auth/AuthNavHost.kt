package com.syme.ui.navigation.auth

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.syme.ui.screen.auth.AuthScreen
import com.syme.ui.viewmodel.LoginViewModel
import com.syme.ui.viewmodel.RegisterViewModel
import com.syme.ui.viewmodel.ResetPasswordViewModel

@Composable
fun AuthNavHost(
    navController: NavHostController
) {
    val registerViewModel: RegisterViewModel = hiltViewModel()
    val loginViewModel: LoginViewModel = hiltViewModel()
    val resetPasswordViewModel: ResetPasswordViewModel = hiltViewModel()

    AuthScreen { contentPadding ->

        NavHost(
            navController = navController,
            startDestination = AuthRoute.Login.route
        ) {
            authNavGraph(
                navController = navController,
                registerViewModel =  registerViewModel,
                resetPasswordViewModel = resetPasswordViewModel,
                loginViewModel = loginViewModel,
                contentPadding = contentPadding // ✅ vrai padding
            )
        }
    }
}