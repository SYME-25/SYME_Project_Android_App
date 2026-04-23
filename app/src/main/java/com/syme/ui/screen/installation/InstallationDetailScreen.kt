package com.syme.ui.screen.installation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syme.R
import com.syme.domain.mapper.imageResId
import com.syme.domain.model.enumeration.Mode
import com.syme.domain.state.UiState
import com.syme.ui.component.animation.ItemDetailHeader
import com.syme.ui.component.compositionlocal.LocalCurrentUserSession
import com.syme.ui.component.dialog.LoadingDialog
import com.syme.ui.screen.installation.components.InstallationForm
import com.syme.ui.snapshot.MessageType
import com.syme.ui.snapshot.globalMessageManager
import com.syme.ui.viewmodel.InstallationViewModel
import com.syme.utils.installationCatalog

@Composable
fun InstallationDetailScreen(
    installationId: String,
    mode: String,
    installationViewModel: InstallationViewModel,
    onBack: () -> Unit,
    contentPadding: PaddingValues
) {
    val ownerId = LocalCurrentUserSession.current?.userId
    val isEditMode = mode == Mode.EDIT.name

    val selected   by installationViewModel.selected.collectAsStateWithLifecycle()
    val saveState  by installationViewModel.saveState.collectAsStateWithLifecycle() // ← NOUVEAU

    // ── Strings pour les messages ────────────────────────────────────────
    val msgCreated = stringResource(R.string.installation_created_success) // à créer
    val msgUpdated = stringResource(R.string.installation_updated_success) // à créer
    val msgError   = stringResource(R.string.installation_save_error)      // à créer

    // ── Réaction au saveState ────────────────────────────────────────────
    LaunchedEffect(saveState) {
        when (saveState) {
            is UiState.Success -> {
                globalMessageManager.showMessage(
                    type = MessageType.SUCCESS,
                    customText = if (isEditMode) msgUpdated else msgCreated
                )
                installationViewModel.resetSaveState()
                onBack()
            }
            is UiState.Error -> {
                globalMessageManager.showMessage(
                    type = MessageType.ERROR,
                    customText = msgError
                )
                installationViewModel.resetSaveState()
            }
            else -> Unit
        }
    }

    LaunchedEffect(installationId, isEditMode) {
        if (isEditMode && !ownerId.isNullOrBlank()) {
            installationViewModel.getById(ownerId, installationId)
        }
    }

    val installation = if (isEditMode) selected
    else installationCatalog.find { it.installationId == installationId }

    if (installation == null) {
        if (isEditMode) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(strokeWidth = 2.dp)
            }
        } else {
            LaunchedEffect(Unit) { onBack() }
        }
        return
    }

    val bgTop    = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    val bgBottom = MaterialTheme.colorScheme.background
    val screenTitle = if (isEditMode)
        stringResource(R.string.dialog_edit_installation_title)
    else
        stringResource(R.string.home_add_installation_title)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(bgTop, bgBottom, bgBottom)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            ItemDetailHeader(
                id      = installation.type.imageResId,
                label   = screenTitle,
                onBack  = onBack
            )
            Surface(
                color          = MaterialTheme.colorScheme.background,
                shape          = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                shadowElevation = 8.dp,
                modifier       = Modifier
                    .fillMaxWidth()
                    .offset(y = (-28).dp)
            ) {
                InstallationForm(
                    item        = installation,
                    isEditMode  = isEditMode,
                    onSaveInstallation = { newInstallation ->
                        // Plus de onBack() ici — géré par LaunchedEffect(saveState)
                        if (isEditMode)
                            installationViewModel.update(ownerId ?: "", newInstallation)
                        else
                            installationViewModel.insert(ownerId ?: "", newInstallation)
                    }
                )
            }
            Spacer(
                modifier = Modifier.height(
                    contentPadding.calculateBottomPadding() + 52.dp
                )
            )
        }

        // ── LoadingDialog par-dessus tout ────────────────────────────────
        LoadingDialog(visible = saveState is UiState.Loading)
    }
}