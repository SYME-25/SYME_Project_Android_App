package com.syme.ui.screen.appliance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.syme.ui.screen.appliance.components.ApplianceForm
import com.syme.ui.snapshot.MessageType
import com.syme.ui.snapshot.globalMessageManager
import com.syme.ui.viewmodel.ApplianceViewModel
import com.syme.ui.viewmodel.CircuitViewModel
import com.syme.utils.applianceCatalog

@Composable
fun ApplianceDetailScreen(
    applianceId: String,
    installationId: String?,
    mode: String,
    circuitViewModel: CircuitViewModel,
    applianceViewModel: ApplianceViewModel,
    onBack: () -> Unit,
    contentPadding: PaddingValues
) {
    val ownerId = LocalCurrentUserSession.current?.userId
    val isEditMode = mode == Mode.EDIT.name

    val circuits by circuitViewModel.circuits.collectAsState()
    val selected by applianceViewModel.selected.collectAsStateWithLifecycle()
    val saveState by applianceViewModel.saveState.collectAsStateWithLifecycle()

    val msgCreated = stringResource(R.string.appliance_created_success)
    val msgUpdated = stringResource(R.string.appliance_updated_success)
    val msgError   = stringResource(R.string.appliance_save_error)

    // 🎯 Gestion du résultat comme Installation
    LaunchedEffect(saveState) {
        when (saveState) {
            is UiState.Success -> {
                globalMessageManager.showMessage(
                    type = MessageType.SUCCESS,
                    customText = if (isEditMode) msgUpdated else msgCreated
                )
                applianceViewModel.resetSaveState()
                onBack()
            }
            is UiState.Error -> {
                globalMessageManager.showMessage(
                    type = MessageType.ERROR,
                    customText = msgError
                )
                applianceViewModel.resetSaveState()
            }
            else -> Unit
        }
    }

    LaunchedEffect(applianceId, installationId, isEditMode){
        if (isEditMode && !ownerId.isNullOrBlank()) {
            applianceViewModel.getById(ownerId, installationId ?: "", applianceId)
        }
    }

    val appliance = if (isEditMode) selected
    else applianceCatalog.find { it.applianceId == applianceId }

    if (appliance == null) {
        if (isEditMode) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(strokeWidth = 2.dp)
            }
        } else {
            LaunchedEffect(Unit) { onBack() }
        }
        return
    }

    val bgTop = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    val bgBottom = MaterialTheme.colorScheme.background

    val screenTitle = if (isEditMode)
        stringResource(R.string.dialog_edit_appliance_title)
    else
        stringResource(R.string.home_add_appliance_title)

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
                id = appliance.type.imageResId,
                label = screenTitle,
                onBack = onBack
            )

            Surface(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-28).dp)
            ) {
                ApplianceForm(
                    item = appliance,
                    circuits = circuits,
                    isEditMode = isEditMode,
                    onSaveAppliance = { newAppliance ->
                        if (isEditMode) {
                            applianceViewModel.update(
                                ownerId ?: "",
                                installationId ?: "",
                                newAppliance
                            )
                        } else {
                            applianceViewModel.insert(
                                ownerId ?: "",
                                installationId ?: "",
                                newAppliance
                            )
                        }
                    }
                )
            }

            Spacer(
                modifier = Modifier.height(
                    contentPadding.calculateBottomPadding() + 52.dp
                )
            )
        }

        LoadingDialog(visible = saveState is UiState.Loading)
    }
}