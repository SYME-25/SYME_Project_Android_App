package com.syme.ui.screen.auth

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest
import com.syme.R
import com.syme.domain.model.ResetPasswordEvent
import com.syme.domain.state.UiState
import com.syme.ui.component.actionbutton.AppButton
import com.syme.ui.component.actionbutton.AppTextButton
import com.syme.ui.component.field.EmailField
import com.syme.ui.navigation.auth.AuthRoute
import com.syme.ui.snapshot.GlobalMessageSnapshot
import com.syme.ui.snapshot.MessageType
import com.syme.ui.snapshot.globalMessageManager
import com.syme.ui.theme.SYMETheme
import com.syme.ui.viewmodel.ResetPasswordViewModel

@Composable
fun ResetPasswordStep1Screen(
    viewModel: ResetPasswordViewModel,
    navController: NavController,
    onBackToLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    var email      by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()

    val resetPasswordLabelText       = stringResource(R.string.password_reset_label)
    val emailText                    = stringResource(R.string.register_email)
    val emailErrorText               = stringResource(R.string.password_reset_email_error)
    val emailNotValidText            = stringResource(R.string.password_reset_email_not_valid)
    val passwordResetBackToLoginText = stringResource(R.string.password_reset_back_to_login)

    // Écoute les événements
    LaunchedEffect(viewModel) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is ResetPasswordEvent.Success -> {
                    globalMessageManager.showMessage(
                        type       = MessageType.SUCCESS,
                        customText = context.getString(event.messageRes)
                    )
                    // Navigue vers step 2 après l'envoi réussi
                    navController.navigate(AuthRoute.ResetPasswordStep2.route)
                }
                is ResetPasswordEvent.Error -> {
                    globalMessageManager.showMessage(
                        type       = MessageType.ERROR,
                        customText = context.getString(event.messageRes)
                    )
                }
            }
        }
    }

    // Nettoie l'état quand on quitte l'écran
    DisposableEffect(Unit) {
        onDispose { viewModel.resetState() }
    }

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuthHeader(
                animationRes = R.raw.password_reset_animation_1,
                title        = resetPasswordLabelText
            )

            AuthFieldsCard {
                EmailField(
                    value         = email,
                    onValueChange = { email = it },
                    label         = emailText,
                    error         = emailError
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AppButton(
                text      = resetPasswordLabelText,
                isLoading = uiState is UiState.Loading,
                onClick   = {
                    emailError = when {
                        email.isBlank() -> emailErrorText
                        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> emailNotValidText
                        else -> ""
                    }
                    if (emailError.isEmpty()) {
                        viewModel.sendResetEmail(email)
                    }
                },
                leadingIcon = {
                    Icon(painterResource(R.drawable.outline_send_24), null)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            AppTextButton(
                text    = passwordResetBackToLoginText,
                onClick = onBackToLogin
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        GlobalMessageSnapshot()
    }
}

@Preview(showBackground = true)
@Composable
fun ResetPasswordStep1ScreenPreview() {
    SYMETheme {
        // Preview sans ViewModel — utilise un NavController local
        ResetPasswordStep1Screen(
            viewModel  = androidx.lifecycle.viewmodel.compose.viewModel(),
            navController = NavController(LocalContext.current)
        )
    }
}