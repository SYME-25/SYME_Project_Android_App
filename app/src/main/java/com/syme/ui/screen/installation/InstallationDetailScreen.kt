package com.syme.ui.screen.installation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.syme.R
import com.syme.domain.mapper.imageResId
import com.syme.ui.component.animation.ItemDetailHeader
import com.syme.ui.component.compositionlocal.LocalCurrentUserSession
import com.syme.ui.screen.installation.components.InstallationForm
import com.syme.ui.viewmodel.InstallationViewModel
import com.syme.utils.installationCatalog
import kotlin.plus

@Composable
fun InstallationDetailScreen(
    installationId: String,
    installationViewModel: InstallationViewModel,
    onBack: () -> Unit,
    contentPadding : PaddingValues
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
            .padding(contentPadding)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            ItemDetailHeader(
                id = installation.type.imageResId,
                label = stringResource(R.string.home_add_installation_title),
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

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(contentPadding.calculateBottomPadding() + 52.dp)
        )
    }
}