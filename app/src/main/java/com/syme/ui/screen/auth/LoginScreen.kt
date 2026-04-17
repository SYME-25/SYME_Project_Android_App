package com.syme.ui.screen.auth

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.syme.domain.model.LoginEvent
import com.syme.ui.component.animation.banner.Banner
import com.syme.ui.navigation.RootRoute
import com.syme.ui.snapshot.MessageType
import com.syme.ui.snapshot.globalMessageManager
import com.syme.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.flow.collectLatest
import com.syme.R
import com.syme.ui.component.actionbutton.AppButton
import com.syme.ui.component.actionbutton.LoginLinksRow
import com.syme.ui.component.field.EmailField
import com.syme.ui.component.field.PasswordField
import com.syme.ui.snapshot.GlobalMessageSnapshot

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToRegister: () -> Unit = {},
    onNavigateToResetPassword: () -> Unit = {}
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    val emailErrorText       = stringResource(R.string.login_email_error)
    val passwordErrorText    = stringResource(R.string.login_password_error)
    val loginLabelText       = stringResource(R.string.login_label)
    val loginEmailText       = stringResource(R.string.login_email)
    val loginPasswordText    = stringResource(R.string.login_password)
    val loginForgetPasswordText = stringResource(R.string.login_forget_password)
    val loginNotMemberText   = stringResource(R.string.login_not_member)
    val loginSignInText      = stringResource(R.string.login_sign_in)
    val emailFormatInvalid   = stringResource(R.string.register_email_invalid_format)

    LaunchedEffect(viewModel) {
        viewModel.loginEvent.collectLatest { event ->
            when (event) {
                is LoginEvent.Success -> {
                    globalMessageManager.showMessage(
                        type = MessageType.SUCCESS,
                        customText = context.getString(event.messageRes)
                    )
                }
                is LoginEvent.Error -> {
                    globalMessageManager.showMessage(
                        type = MessageType.ERROR,
                        customText = context.getString(event.messageRes, event.arg)
                    )
                }
            }
        }
    }

    // Dégradé de fond adaptatif clair/sombre
    val bgTop    = MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)
    val bgBottom = MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(bgTop, bgBottom, bgBottom))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Animation ──────────────────────────────────────────
            Banner(
                id = R.raw.login_animation,
            )

            // ── Titre ──────────────────────────────────────────────
            Text(
                text = loginLabelText,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // ── Carte champs ───────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EmailField(
                    value = email,
                    onValueChange = { email = it },
                    label = loginEmailText,
                    error = emailError
                )
                PasswordField(
                    value = password,
                    onValueChange = { password = it },
                    label = loginPasswordText,
                    error = passwordError,
                    passwordVisible = passwordVisible,
                    onToggleVisibility = { passwordVisible = !passwordVisible }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Bouton ─────────────────────────────────────────────
            AppButton(
                text = loginLabelText,
                onClick = {
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

            Spacer(modifier = Modifier.height(24.dp))
        }

        GlobalMessageSnapshot()
    }
}