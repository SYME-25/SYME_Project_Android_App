package com.syme.ui.screen.auth

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
import com.syme.R
import com.syme.ui.component.actionbutton.AppButton
import com.syme.ui.component.actionbutton.AppTextButton
import com.syme.ui.snapshot.GlobalMessageSnapshot
import com.syme.ui.theme.SYMETheme

@Composable
fun ResetPasswordStep2Screen(
    navController: NavController,
    onResetComplete: () -> Unit = {},
    contentPadding: PaddingValues
) {
    val submitText      = stringResource(R.string.password_new_submit)
    val backToLoginText = stringResource(R.string.password_new_back_to_login)
    val newLabelText    = stringResource(R.string.password_new_label)
    val subtitleText    = stringResource(R.string.password_reset_check_email)

    AuthBackground (contentPadding = contentPadding) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuthHeader(
                animationRes = R.raw.password_reset_animation_2,
                title        = newLabelText
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Message d'instruction
            AuthFieldsCard {
                Text(
                    text     = stringResource(R.string.password_reset_instructions),
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AppButton(
                text    = submitText,
                onClick = onResetComplete,
                leadingIcon = {
                    Icon(painterResource(R.drawable.outline_send_24), null)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            AppTextButton(
                text    = backToLoginText,
                onClick = onResetComplete
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        GlobalMessageSnapshot(paddingValues = contentPadding)
    }
}