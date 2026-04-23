package com.syme.ui.screen.auth

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Language
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.syme.domain.model.LoginEvent
import com.syme.ui.component.animation.banner.Banner
import com.syme.ui.navigation.RootRoute
import com.syme.ui.snapshot.MessageType
import com.syme.ui.snapshot.globalMessageManager
import com.syme.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.flow.collectLatest
import com.syme.R
import com.syme.domain.mapper.labelRes
import com.syme.domain.model.enumeration.AppLanguage
import com.syme.domain.state.UiState
import com.syme.ui.component.actionbutton.AppButton
import com.syme.ui.component.actionbutton.AppIconButton
import com.syme.ui.component.actionbutton.LoginLinksRow
import com.syme.ui.component.dialog.LoadingDialog
import com.syme.ui.component.field.EmailField
import com.syme.ui.component.field.PasswordField
import com.syme.ui.component.text.Title
import com.syme.ui.snapshot.GlobalMessageSnapshot
import com.syme.ui.viewmodel.SettingsViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToRegister: () -> Unit = {},
    onNavigateToResetPassword: () -> Unit = {},
    contentPadding: PaddingValues
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    val isLoading = viewModel.uiState.collectAsState().value is UiState.Loading

    val emailErrorText       = stringResource(R.string.login_email_error)
    val passwordErrorText    = stringResource(R.string.login_password_error)
    val loginLabelText       = stringResource(R.string.login_label)
    val loginEmailText       = stringResource(R.string.login_email)
    val loginPasswordText    = stringResource(R.string.login_password)
    val loginForgetPasswordText = stringResource(R.string.login_forget_password)
    val loginNotMemberText   = stringResource(R.string.login_not_member)
    val loginSignInText      = stringResource(R.string.login_sign_in)
    val emailFormatInvalid   = stringResource(R.string.register_email_invalid_format)

    val language by settingsViewModel.language.collectAsStateWithLifecycle()
    var showLanguagePopup by remember { mutableStateOf(false) }

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
            .padding(contentPadding)
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

            AuthHeader(
                animationRes = R.raw.login_animation,
                title = loginLabelText
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

        // ── Bouton langue ── coin bas-droit ────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
        ) {
            AppIconButton(
                icon               = Icons.Rounded.Language,
                contentDescription = "Langue",
                onClick            = { showLanguagePopup = !showLanguagePopup }
            )

            // ── Popup ancrée au bouton ─────────────────────────────
            if (showLanguagePopup) {
                Popup(
                    alignment  = Alignment.BottomEnd,
                    offset     = IntOffset(x = 0, y = -48),   // juste au-dessus du bouton
                    properties = PopupProperties(focusable = true),
                    onDismissRequest = { showLanguagePopup = false }
                ) {
                    Surface(
                        shape       = RoundedCornerShape(14.dp),
                        tonalElevation = 4.dp,
                        shadowElevation = 4.dp,
                        color       = MaterialTheme.colorScheme.surface,
                        modifier    = Modifier.width(180.dp)
                    ) {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            AppLanguage.entries.forEach { option ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            settingsViewModel.setLanguage(option)
                                            showLanguagePopup = false
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment   = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    RadioButton(
                                        selected = language == option,
                                        onClick  = {
                                            settingsViewModel.setLanguage(option)
                                            showLanguagePopup = false
                                        }
                                    )
                                    Text(
                                        text  = stringResource(option.labelRes),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        LoadingDialog(visible = isLoading)
        GlobalMessageSnapshot(paddingValues = contentPadding)
    }
}