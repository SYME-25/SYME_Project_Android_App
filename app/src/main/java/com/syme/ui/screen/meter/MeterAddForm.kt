package com.syme.ui.screen.meter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.R
import com.syme.ui.component.actionbutton.AppButton
import com.syme.ui.component.field.NameField
import com.syme.ui.component.field.NumberField
import com.syme.ui.component.field.PasswordField

@Composable
fun MeterAddForm(
    onSubmit: (meterId: String, securityCode: String) -> Unit
) {
    var meterId by remember { mutableStateOf("") }
    var securityCode by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var meterIdError by remember { mutableStateOf("") }
    var securityCodeError by remember { mutableStateOf("") }

    val meterIdErrorMsg = stringResource(R.string.meter_id_required_error)
    val securityCodeErrorMsg = stringResource(R.string.meter_security_code_required_error)

    fun validate(): Boolean {
        meterIdError = if (meterId.isBlank()) meterIdErrorMsg else ""
        securityCodeError = if (securityCode.isBlank()) securityCodeErrorMsg else ""
        return meterIdError.isEmpty() && securityCodeError.isEmpty()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            Text(
                text = stringResource(R.string.meter_add_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp)
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Meter ID field
        item {
            NameField(
                value = meterId,
                onValueChange = { meterId = it },
                label = stringResource(R.string.meter_id_label),
                error = meterIdError
            )
        }

        // Security code field
        item {
            PasswordField(
                value = securityCode,
                onValueChange = { securityCode = it },
                label = stringResource(R.string.meter_security_code_label),
                error = securityCodeError,
                passwordVisible = passwordVisible,
                onToggleVisibility = { passwordVisible = !passwordVisible }
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Submit button
        item {
            AppButton(
                text = stringResource(R.string.meter_add_button),
                onClick = {
                    if (validate()) {
                        onSubmit(meterId, securityCode)
                    }
                }
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Preview(showBackground = true)
@Composable
fun MeterAddFormPreview() {
    MeterAddForm { meterId, securityCode ->
        println("Meter ID: $meterId, Security Code: $securityCode")
    }
}

