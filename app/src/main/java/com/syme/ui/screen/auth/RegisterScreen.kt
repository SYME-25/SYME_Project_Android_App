package com.syme.ui.screen.auth

import android.util.Patterns
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.syme.R
import com.syme.ui.component.actionbutton.AppButton
import com.syme.ui.component.actionbutton.AppTextButton
import com.syme.ui.component.animation.Animation
import com.syme.ui.component.field.EmailField
import com.syme.ui.component.field.NameField
import com.syme.ui.component.field.PasswordField
import com.syme.ui.navigation.auth.AuthRoute
import com.syme.ui.theme.SYMETheme

@Composable
fun RegisterScreen(
    navController: NavController,
    onNavigateBack : () -> Unit = {},
    onRegistrationSuccess : () -> Unit = {},
    paddingValues: PaddingValues
) {

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    val emailErrorText = stringResource(R.string.register_email_error)
    val passwordErrorText = stringResource(R.string.register_password_error)
    val registerLabelText = stringResource(R.string.register_label)
    val registerEmailText = stringResource(R.string.register_email)
    val registerPasswordText = stringResource(R.string.register_password)
    val registerConfirmPasswordText = stringResource(R.string.register_confirm_password)
    val confirmPasswordErrorText = stringResource(R.string.register_confirm_password_error)
    val confirmPasswordRequiredText = stringResource(R.string.register_confirm_password_required)
    val redisterEmailInvalidText = stringResource(R.string.register_invalid_email_error)
    val registerNameText = stringResource(R.string.register_name)
    val registerNameErrorText = stringResource(R.string.register_name_error)
    val registerEnterDetailsText = stringResource(R.string.register_enter_details)
    val registerAlreadyAccountText = stringResource(R.string.register_already_account)


    LazyColumn (
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item { Animation(R.raw.registration_animation) }

        item {
            Text(
                text = registerLabelText,
                fontSize = 24.sp,
                modifier = Modifier.fillMaxWidth().padding(start = 25.dp, top = 15.dp),
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.ExtraBold
            )
        }

        item { Spacer(modifier = Modifier.height(10.dp)) }

        item {
            Text(
                text = registerEnterDetailsText,
                fontSize = 18.sp,
                modifier = Modifier.fillMaxWidth().padding(start = 25.dp, top = 15.dp),
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Light
            )
        }

        item { Spacer(modifier = Modifier.height(10.dp)) }

        item {
            NameField(
                value = name,
                onValueChange = { name = it },
                label = registerNameText,
                error = nameError
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            EmailField(
                value = email,
                onValueChange = { email = it },
                label = registerEmailText,
                error = emailError
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            PasswordField(
                value = password,
                onValueChange = { password = it },
                label = registerPasswordText,
                passwordVisible = passwordVisible,
                onToggleVisibility = {
                    passwordVisible = !passwordVisible
                },
                error = passwordError
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            PasswordField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = registerConfirmPasswordText,
                passwordVisible = confirmPasswordVisible,
                onToggleVisibility = {
                    confirmPasswordVisible =
                        !confirmPasswordVisible
                },
                error = confirmPasswordError
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
            AppButton(
                text = registerLabelText,
                onClick = {
                    nameError =
                        if (name.isBlank()) registerNameErrorText else ""
                    emailError =
                        if (email.isBlank()) emailErrorText
                        else if (!Patterns.EMAIL_ADDRESS.matcher(
                                email
                            ).matches()
                        )
                            redisterEmailInvalidText
                        else ""

                    passwordError =
                        if (password.isBlank()) passwordErrorText else ""
                    confirmPasswordError =
                        if (confirmPassword.isBlank()) confirmPasswordRequiredText
                        else if (password != confirmPassword) confirmPasswordErrorText
                        else ""

                    if (nameError.isEmpty() && emailError.isEmpty() && passwordError.isEmpty() && confirmPasswordError.isEmpty()) {
                        // TODO: Handle login
                    }
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.outline_send_24),
                        contentDescription = null
                    )
                }
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            AppTextButton(
                text = registerAlreadyAccountText,
                onClick = onNavigateBack

            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    SYMETheme() {
        RegisterScreen(
            navController = NavController(LocalContext.current),
            paddingValues = PaddingValues(0.dp)
        )
    }
}