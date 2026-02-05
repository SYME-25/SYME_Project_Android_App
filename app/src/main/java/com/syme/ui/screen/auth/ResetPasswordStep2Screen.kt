package com.syme.ui.screen.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.syme.ui.component.actionbutton.AppButton
import com.syme.ui.component.actionbutton.AppTextButton
import com.syme.ui.component.animation.Animation
import com.syme.ui.component.field.PasswordField
import com.syme.ui.navigation.auth.AuthRoute
import com.syme.ui.theme.SYMETheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordStep2Screen(
    navController: NavController,
    onResetComplete : () -> Unit = {}
) {

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    val newLabelText = stringResource(R.string.password_new_label)
    val newPasswordText = stringResource(R.string.password_new_password)
    val newPasswordErrorText = stringResource(R.string.password_new_password_error)
    val confirmPasswordText = stringResource(R.string.password_new_confirm_password)
    val confirmPasswordErrorText = stringResource(R.string.password_new_confirm_password_error)
    val confirmPasswordNotMatchText = stringResource(R.string.password_new_confirm_password_not_match)
    val backToLoginText = stringResource(R.string.password_new_back_to_login)
    val submitText = stringResource(R.string.password_new_submit)


    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Animation(R.raw.password_reset_animation_2)

        Text(
            text = newLabelText,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        PasswordField(
            value = password,
            onValueChange = { password = it },
            label = newPasswordText,
            error = passwordError,
            passwordVisible = passwordVisible,
            onToggleVisibility = { passwordVisible = !passwordVisible }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PasswordField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = confirmPasswordText,
            error = confirmPasswordError,
            passwordVisible = confirmPasswordVisible,
            onToggleVisibility = { confirmPasswordVisible = !confirmPasswordVisible }
        )

        Spacer(modifier = Modifier.height(24.dp))

        AppButton(
            text = submitText,
            onClick = {
                passwordError = if (password.isBlank()) newPasswordErrorText else ""
                confirmPasswordError =
                    if (confirmPassword.isBlank()) confirmPasswordErrorText
                    else if (password != confirmPassword) confirmPasswordNotMatchText
                    else ""
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.outline_send_24),
                    contentDescription = null
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppTextButton(
            text = backToLoginText,
            onClick = onResetComplete
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ResetPassword_Step_2_ScreenPreview() {
    val navController = NavController(LocalContext.current)
    SYMETheme() {
        ResetPasswordStep2Screen(navController = navController)
    }
}