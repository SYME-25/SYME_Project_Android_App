package com.syme.ui.screen.installation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syme.domain.mapper.imageResId
import com.syme.domain.model.Installation
import com.syme.ui.component.compositionlocal.LocalCurrentUserSession
import com.syme.ui.viewmodel.InstallationViewModel

import com.syme.utils.installationCatalog

@Composable
fun InstallationDetailScreen(
    installationId: String,
    installationViewModel: InstallationViewModel,
    onBack: () -> Unit
) {

    val ownerId = LocalCurrentUserSession.current?.userId

    val installation = installationCatalog.find {
        it.installationId == installationId
    }

    // Sécurité : si l'id n'existe pas, on repart en arrière
    if (installation == null) {
        onBack()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            InstallationDetailHeader(
                id = installation.type.imageResId,
                onBack = onBack
            )

            Surface(
                color = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 400.dp)
            ) {
                InstallationDetailBody(
                    item = installation,
                    onSaveInstallation = { newInstallation ->
                        installationViewModel.insert(ownerId ?: "", newInstallation)
                        onBack()
                    }
                )
            }
        }
    }
}
