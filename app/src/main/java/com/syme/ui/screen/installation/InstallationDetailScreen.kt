package com.syme.ui.screen.installation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syme.domain.mapper.imageResId
import com.syme.ui.component.animation.ItemDetailHeader
import com.syme.ui.component.compositionlocal.LocalCurrentUserSession
import com.syme.ui.screen.installation.components.InstallationForm
import com.syme.ui.viewmodel.InstallationViewModel
import com.syme.utils.installationCatalog

@Composable
fun InstallationDetailScreen(
    installationId: String,
    installationViewModel: InstallationViewModel,
    onBack: () -> Unit
) {
    val ownerId = LocalCurrentUserSession.current?.userId
    val installation = installationCatalog.find { it.installationId == installationId }

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
            ItemDetailHeader(
                id = installation.type.imageResId,
                onBack = onBack
            )
            // Sheet content overlapping the header image
            Surface(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 360.dp)
            ) {
                InstallationForm(
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