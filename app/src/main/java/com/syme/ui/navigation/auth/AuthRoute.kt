package com.syme.ui.navigation.auth

sealed class AuthRoute(val route: String) {
    object Login : AuthRoute("login")
    object Register : AuthRoute("register")
    object ResetPasswordStep1 : AuthRoute("reset_password_step1")
    object ResetPasswordStep2 : AuthRoute("reset_password_step2")
}