package com.syme.ui.screen.appliance

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
import com.syme.domain.model.Appliance
import com.syme.ui.component.card.ApplianceRow

@Composable
fun UserAppliancesList(items: List<Appliance>, onClick: (Appliance) -> Unit) {

    val yourAppliancesMsg = stringResource(R.string.home_your_appliances)
    val noAppliancesFoundMsg = stringResource(R.string.home_no_appliances_found)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 24.dp, end = 16.dp)
    ) {
        Text(
            text = yourAppliancesMsg,
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
                Text(
                    text = noAppliancesFoundMsg,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        } else {
            ApplianceRow(
                items = items,
                onClick = onClick,
                contentAction = { Text("") } // Placeholder si action supplémentaire
            )
        }
    }
}
