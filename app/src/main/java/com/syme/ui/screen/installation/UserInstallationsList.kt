package com.syme.ui.screen.installation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.R
import com.syme.domain.model.Installation
import com.syme.ui.component.animation.Animation
import com.syme.ui.component.card.InstallationRow

@Composable
fun UserInstallationsList(items: List<Installation>, onClick: (Installation) -> Unit){

    val yourInstallationsMsg = stringResource(R.string.home_your_installations)
    val noInstallationsFoundMsg = stringResource(R.string.home_no_installations_found)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 24.dp, end = 16.dp)
    ) {
        Text(
            text = yourInstallationsMsg,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (items.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Animation(R.raw.blue_house)
                Text(
                    text = noInstallationsFoundMsg,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        } else {
            InstallationRow(items, onClick = onClick, contentAction ={ Text("") })
        }
    }
}
