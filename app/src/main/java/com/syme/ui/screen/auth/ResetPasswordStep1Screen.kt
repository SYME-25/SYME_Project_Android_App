package com.syme.ui.screen.auth

import android.util.Patterns
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.syme.ui.component.field.EmailField
import com.syme.ui.navigation.auth.AuthRoute
import com.syme.ui.theme.SYMETheme

@Composable
fun ResetPasswordStep1Screen(
    navController: NavController,
    onBackToLogin : () -> Unit = {},
    onNextStep : () -> Unit = {},
    paddingValues: PaddingValues
) {

    var email by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf("") }

    val resetPasswordLabelText = stringResource(R.string.password_reset_label)
    val emailText = stringResource(R.string.register_email)
    val emailErrorText = stringResource(R.string.password_reset_email_error)
    val emailNotValidText = stringResource(R.string.password_reset_email_not_valid)
    val passwordResetBackToLoginText = stringResource(R.string.password_reset_back_to_login)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Animation(R.raw.password_reset_animation_1)

        Text(
            text = resetPasswordLabelText,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        EmailField(
            value = email,
            onValueChange = { email = it },
            label = emailText,
            error = emailError
        )

        Spacer(modifier = Modifier.height(24.dp))

        AppButton(
            text = resetPasswordLabelText,
            onClick = {
                emailError = if (email.isBlank()) emailErrorText
                else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                    emailNotValidText
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
            text = passwordResetBackToLoginText,
            onClick = onBackToLogin
        )

        AppTextButton(
            text = "Screen_2",
            onClick = {
                navController.navigate(AuthRoute.ResetPasswordStep2.route)
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ResetPasswordStepStep1ScreenPreview() {
    val navController = NavController(LocalContext.current)
    SYMETheme() {
        ResetPasswordStep1Screen(navController = navController, paddingValues = PaddingValues())
    }
}