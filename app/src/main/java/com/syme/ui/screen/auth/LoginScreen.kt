package com.syme.ui.screen.auth

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.syme.R
import com.syme.domain.model.LoginEvent
import com.syme.ui.component.actionbutton.AppButton
import com.syme.ui.component.animation.Animation
import com.syme.ui.component.field.EmailField
import com.syme.ui.component.field.PasswordField
import com.syme.ui.component.link.LoginLinksRow
import com.syme.ui.navigation.RootRoute
import com.syme.ui.snapshot.GlobalMessageSnapshot
import com.syme.ui.snapshot.MessageAction
import com.syme.ui.snapshot.MessageType
import com.syme.ui.snapshot.globalMessageManager
import com.syme.ui.state.UiState
import com.syme.ui.theme.SYMETheme
import com.syme.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    navController: NavController,
    onNavigateToRegister: () -> Unit = {},
    onNavigateToResetPassword: () -> Unit = {}
) {

    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    val emailErrorText = stringResource(R.string.login_email_error)
    val passwordErrorText = stringResource(R.string.login_password_error)
    val loginLabelText = stringResource(R.string.login_label)
    val loginEmailText = stringResource(R.string.login_email)
    val loginPasswordText = stringResource(R.string.login_password)
    val loginForgetPasswordText = stringResource(R.string.login_forget_password)
    val loginNotMemberText = stringResource(R.string.login_not_member)
    val loginSignInText = stringResource(R.string.login_sign_in)
    val emailFormatInvalid = stringResource(R.string.register_email_invalid_format)

    val uiState by viewModel.uiState.collectAsState()

    // 🎯 Écoute les événements pour afficher les messages globaux
    LaunchedEffect(viewModel) {
        viewModel.loginEvent.collectLatest { event ->
            when (event) {
                is LoginEvent.Success -> {
                    globalMessageManager.showMessage(
                        type = MessageType.SUCCESS,
                        customText = context.getString(event.messageRes)
                    )
                    navController.navigate(RootRoute.Main) {
                        popUpTo(RootRoute.Auth) { inclusive = true }
                    }
                }

                is LoginEvent.Error -> {
                    globalMessageManager.showMessage(
                        type = MessageType.ERROR,
                        customText = context.getString(
                            event.messageRes,
                            event.arg
                        )
                    )
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Animation(R.raw.login_animation)

        Text(
            text = loginLabelText,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        EmailField(
            value = email,
            onValueChange = { email = it },
            label = loginEmailText,
            error = emailError
        )

        Spacer(modifier = Modifier.height(8.dp))

        PasswordField(
            value = password,
            onValueChange = { password = it },
            label = loginPasswordText,
            error = passwordError,
            passwordVisible = passwordVisible,
            onToggleVisibility = { passwordVisible = !passwordVisible }
        )

        Spacer(modifier = Modifier.height(24.dp))

        AppButton(
            text = loginLabelText,
            onClick = {
                // Validation rapide
                emailError = when {
                    email.isBlank() -> emailErrorText
                    !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> emailFormatInvalid
                    else -> ""
                }
                passwordError = if (password.isBlank()) passwordErrorText else ""

                if (emailError.isEmpty() && passwordError.isEmpty()) {
                    viewModel.login(email, password)
                }
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.baseline_login_24),
                    contentDescription = null
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LoginLinksRow(
            forgetPasswordText = loginForgetPasswordText,
            notMemberText = loginNotMemberText,
            signInText = loginSignInText,
            onResetPassword = onNavigateToResetPassword,
            onRegister = onNavigateToRegister
        )
    }

    GlobalMessageSnapshot()
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    SYMETheme {
        // Preview sans vrai ViewModel
    }
}
