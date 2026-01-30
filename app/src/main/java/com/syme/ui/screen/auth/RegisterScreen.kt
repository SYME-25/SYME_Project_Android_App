package com.syme.ui.screen.auth

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.syme.R
import com.syme.ui.component.actionbutton.AppButton
import com.syme.ui.component.actionbutton.AppCheckbox
import com.syme.ui.component.actionbutton.AppTextButton
import com.syme.ui.component.animation.Animation
import com.syme.ui.component.field.*
import com.syme.ui.theme.SYMETheme
import com.syme.utils.TimeUtils
import com.syme.viewmodel.RegisterViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RegisterScreen(
    navController: NavController,
    onNavigateBack: () -> Unit = {},
    onRegistrationSuccess: () -> Unit = {},
    paddingValues: PaddingValues
) {
    val context = LocalContext.current
    val viewModel: RegisterViewModel = viewModel()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var birthdayTimestamp by remember { mutableStateOf<Long?>(null) }
    var gender by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var firstNameError by remember { mutableStateOf("") }
    var lastNameError by remember { mutableStateOf("") }
    var birthdayError by remember { mutableStateOf("") }
    var genderError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var addressError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    var acceptPolicy by remember { mutableStateOf(false) }
    var acceptPolicyError by remember { mutableStateOf("") }

    val genderItems = listOf(
        stringResource(R.string.register_gender_male),
        stringResource(R.string.register_gender_female))

    val dateFormatter = TimeUtils.dateFormat

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            birthdayTimestamp = calendar.timeInMillis
            birthday = dateFormatter.format(calendar.time)
        },
        2000, 0, 1
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item { Animation(R.raw.registration_animation) }

        item {
            Text(
                text = stringResource(R.string.register_label),
                fontSize = 24.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 25.dp, top = 15.dp),
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.ExtraBold
            )
        }

        item {
            Text(
                text = stringResource(R.string.register_enter_details),
                fontSize = 18.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 25.dp, top = 8.dp),
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Light
            )
        }

        item { NameField(firstName, { firstName = it }, stringResource(R.string.register_firstname), firstNameError) }
        item { NameField(lastName, { lastName = it }, stringResource(R.string.register_lastname), lastNameError) }

        item {
            DateField(
                value = birthday,
                onValueChange = {},
                label = stringResource(R.string.register_birthday),
                error = birthdayError
            )
            LaunchedEffect(Unit) {
                // clickable overlay
            }
        }

        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(60.dp)
                    .clickable { datePickerDialog.show() }
            )
        }

        item {
            DropdownField(
                value = gender,
                onValueChange = { gender = it },
                label = stringResource(R.string.register_gender),
                error = genderError,
                items = genderItems
            )
        }

        item { NumberField(phone, { phone = it }, stringResource(R.string.register_phone), phoneError) }

        item { NameField(address, { address = it }, stringResource(R.string.register_address), addressError) }

        item { EmailField(email, { email = it }, stringResource(R.string.register_email), emailError) }

        item {
            PasswordField(
                value = password,
                onValueChange = { password = it },
                label = stringResource(R.string.register_password),
                error = passwordError,
                passwordVisible = passwordVisible,
                onToggleVisibility = { passwordVisible = !passwordVisible }
            )
        }

        item {
            PasswordField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = stringResource(R.string.register_confirm_password),
                error = confirmPasswordError,
                passwordVisible = confirmPasswordVisible,
                onToggleVisibility = { confirmPasswordVisible = !confirmPasswordVisible }
            )
        }

        item {
            AppCheckbox(
                checked = acceptPolicy,
                onCheckedChange = { acceptPolicy = it },
                label = stringResource(R.string.register_accept_policy),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )
        }

        if (acceptPolicyError.isNotEmpty()) {
            item {
                Text(
                    text = acceptPolicyError,
                    color = androidx.compose.ui.graphics.Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp)
                )
            }
        }

        if (acceptPolicyError.isNotEmpty()) {
            item {
                Text(
                    text = acceptPolicyError,
                    color = androidx.compose.ui.graphics.Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp)
                )
            }
        }


        item {
            AppButton(
                text = stringResource(R.string.register_label),
                leadingIcon = {
                    Icon(painterResource(R.drawable.outline_send_24), null)
                },
                onClick = {
                    firstNameError = if (firstName.isBlank()) stringResource(R.string.register_firstname_error) else ""
                    lastNameError = if (lastName.isBlank()) stringResource(R.string.register_lastname_error) else ""
                    birthdayError = if (birthdayTimestamp == null) stringResource(R.string.register_birthday_error) else ""
                    genderError = if (gender.isBlank()) stringResource(R.string.register_gender_error) else ""
                    phoneError = if (phone.isBlank()) stringResource(R.string.register_phone_error) else ""
                    addressError = if (address.isBlank()) stringResource(R.string.register_address_error) else ""
                    emailError =
                        if (email.isBlank()) stringResource(R.string.register_email_error)
                        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
                            stringResource(R.string.register_invalid_email_error)
                        else ""
                    passwordError = if (password.isBlank()) stringResource(R.string.register_password_error) else ""
                    confirmPasswordError =
                        if (confirmPassword.isBlank()) stringResource(R.string.register_confirm_password_required)
                        else if (password != confirmPassword) stringResource(R.string.register_confirm_password_error)
                        else ""
                    acceptPolicyError =
                        if (!acceptPolicy) stringResource(R.string.register_accept_policy_error) else ""

                    if (
                        firstNameError.isEmpty() &&
                        lastNameError.isEmpty() &&
                        birthdayError.isEmpty() &&
                        genderError.isEmpty() &&
                        phoneError.isEmpty() &&
                        addressError.isEmpty() &&
                        emailError.isEmpty() &&
                        passwordError.isEmpty() &&
                        confirmPasswordError.isEmpty() &&
                        acceptPolicyError.isEmpty()
                    ) {
                        viewModel.register(
                            firstName = firstName,
                            lastName = lastName,
                            birthday = birthdayTimestamp,
                            gender = gender,
                            phone = phone,
                            address = address,
                            email = email,
                            password = password,
                            onSuccess = { onRegistrationSuccess() },
                            onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                        )

                    }
                }
            )
        }

        item {
            AppTextButton(
                text = stringResource(R.string.register_already_account),
                onClick = onNavigateBack
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    SYMETheme {
        RegisterScreen(
            navController = NavController(LocalContext.current),
            paddingValues = PaddingValues(0.dp)
        )
    }
}
