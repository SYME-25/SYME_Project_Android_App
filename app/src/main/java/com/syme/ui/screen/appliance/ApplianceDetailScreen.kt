package com.syme.ui.screen.appliance

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.syme.domain.mapper.imageResId
import com.syme.ui.component.animation.ItemDetailHeader
import com.syme.ui.component.compositionlocal.LocalCurrentUserSession
import com.syme.ui.viewmodel.ApplianceViewModel
import com.syme.utils.applianceCatalog
import com.syme.ui.viewmodel.CircuitViewModel
import com.syme.R

@Composable
fun ApplianceDetailScreen(
    applianceId: String,
    installationId: String?,
    circuitViewModel: CircuitViewModel,
    applianceViewModel: ApplianceViewModel,
    onBack: () -> Unit,
    contentPadding : PaddingValues
) {

    val ownerId = LocalCurrentUserSession.current?.userId
    val circuits = circuitViewModel.circuits.collectAsState().value

    val appliance = applianceCatalog.find {
        it.applianceId == applianceId
    }

    // Sécurité : si l'id n'existe pas, on repart en arrière
    if (appliance == null) {
        onBack()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {

            ItemDetailHeader(
                id = appliance.type.imageResId,
                label = stringResource(R.string.home_add_appliance_title),
                onBack = onBack
            )

            Surface(
                color = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 400.dp)
            ) {
                ApplianceForm(
                    item = appliance,
                    circuits = circuits,
                    onSaveAppliance = { newAppliance ->
                        applianceViewModel.insert(ownerId ?: "", installationId ?: "", newAppliance)
                        onBack()
                    }
                )
            }
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(contentPadding.calculateBottomPadding() + 52.dp)
        )
    }
}
