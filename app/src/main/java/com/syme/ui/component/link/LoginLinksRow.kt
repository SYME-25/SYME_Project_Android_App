package com.syme.ui.component.link

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoginLinksRow(
    forgetPasswordText: String,
    notMemberText: String,
    signInText: String,
    onResetPassword: () -> Unit,
    onRegister: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Text(
            text = forgetPasswordText,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { onResetPassword() }
        )

        Spacer(modifier = Modifier.height(50.dp))

        Row {
            Text(text = notMemberText)
            Spacer(modifier = Modifier.padding(4.dp))
            Text(
                text = signInText,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onRegister() }
            )
        }
    }
}
