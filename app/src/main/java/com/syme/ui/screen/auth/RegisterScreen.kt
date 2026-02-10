package com.syme.ui.screen.auth

import android.app.DatePickerDialog
import android.util.Patterns
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.syme.R
import com.syme.domain.model.RegisterEvent
import com.syme.ui.component.actionbutton.AppButton
import com.syme.ui.component.actionbutton.AppCheckbox
import com.syme.ui.component.actionbutton.AppTextButton
import com.syme.ui.component.animation.Animation
import com.syme.ui.component.field.*
import com.syme.ui.navigation.auth.AuthRoute
import com.syme.ui.snapshot.GlobalMessageSnapshot
import com.syme.ui.snapshot.MessageType
import com.syme.ui.snapshot.globalMessageManager
import com.syme.ui.viewmodel.RegisterViewModel
import com.syme.utils.RegexUtils
import com.syme.utils.TimeUtils
import kotlinx.coroutines.flow.collectLatest
import java.util.Calendar

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    navController: NavController,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current

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

    // 🎯 Écoute les events comme Login
    LaunchedEffect(viewModel) {
        viewModel.registerEvent.collectLatest { event ->
            when (event) {
                is RegisterEvent.Success -> {
                    globalMessageManager.showMessage(
                        type = MessageType.SUCCESS,
                        customText = context.getString(event.messageRes)
                    )

                    navController.navigate(AuthRoute.Login.route) {
                        popUpTo(AuthRoute.Register.route) { inclusive = true }
                    }
                }

                is RegisterEvent.Error -> {
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

    val genderItems = listOf(
        stringResource(R.string.register_gender_male),
        stringResource(R.string.register_gender_female)
    )

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

    // Strings erreurs
    val firstNameErrorMsg = stringResource(R.string.register_firstname_error)
    val lastNameErrorMsg = stringResource(R.string.register_lastname_error)
    val birthdayErrorMsg = stringResource(R.string.register_birthday_error)
    val genderErrorMsg = stringResource(R.string.register_gender_error)
    val phoneErrorMsg = stringResource(R.string.register_phone_error)
    val phoneInvalidFormatMsg = stringResource(R.string.register_phone_invalid_format)
    val addressErrorMsg = stringResource(R.string.register_address_error)
    val emailErrorMsg = stringResource(R.string.register_email_error)
    val invalidEmailErrorMsg = stringResource(R.string.register_invalid_email_error)
    val passwordErrorMsg = stringResource(R.string.register_password_error)
    val confirmPasswordRequiredMsg = stringResource(R.string.register_confirm_password_required)
    val confirmPasswordErrorMsg = stringResource(R.string.register_confirm_password_error)
    val acceptPolicyErrorMsg = stringResource(R.string.register_accept_policy_error)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item { Spacer(modifier = Modifier.height(24.dp)) }
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

        item { Spacer(modifier = Modifier.height(24.dp)) }

        item { NameField(firstName, { firstName = it }, stringResource(R.string.register_firstname), firstNameError) }
        item { NameField(lastName, { lastName = it }, stringResource(R.string.register_lastname), lastNameError) }

        item {
            DateField(
                value = birthday,
                label = stringResource(R.string.register_birthday),
                error = birthdayError,
                onClick = { datePickerDialog.show() }
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

        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            AppButton(
                text = stringResource(R.string.register_label),
                leadingIcon = {
                    Icon(painterResource(R.drawable.outline_send_24), null)
                },
                onClick = {
                    firstNameError = if (firstName.isBlank()) firstNameErrorMsg else ""
                    lastNameError = if (lastName.isBlank()) lastNameErrorMsg else ""
                    birthdayError = if (birthdayTimestamp == null) birthdayErrorMsg else ""
                    genderError = if (gender.isBlank()) genderErrorMsg else ""
                    phoneError = if (phone.isBlank()) phoneErrorMsg else {
                        if (!RegexUtils.congoPhoneRegex.matches(phone)) phoneInvalidFormatMsg else ""
                    }
                    addressError = if (address.isBlank()) addressErrorMsg else ""
                    emailError =
                        if (email.isBlank()) emailErrorMsg
                        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                            invalidEmailErrorMsg
                        else ""
                    passwordError = if (password.isBlank()) passwordErrorMsg else ""
                    confirmPasswordError =
                        if (confirmPassword.isBlank()) confirmPasswordRequiredMsg
                        else if (password != confirmPassword) confirmPasswordErrorMsg
                        else ""
                    acceptPolicyError =
                        if (!acceptPolicy) acceptPolicyErrorMsg else ""

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
                            password = password
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

        item { Spacer(modifier = Modifier.height(44.dp)) }
    }

    GlobalMessageSnapshot()
}
