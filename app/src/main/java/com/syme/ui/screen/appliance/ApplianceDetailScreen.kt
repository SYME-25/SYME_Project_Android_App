package com.syme.ui.screen.appliance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.syme.R
import com.syme.domain.mapper.imageResId
import com.syme.ui.component.animation.ItemDetailHeader
import com.syme.ui.component.compositionlocal.LocalCurrentUserSession
import com.syme.ui.screen.appliance.components.ApplianceForm
import com.syme.ui.viewmodel.ApplianceViewModel
import com.syme.ui.viewmodel.CircuitViewModel
import com.syme.utils.applianceCatalog

@Composable
fun ApplianceDetailScreen(
    applianceId: String,
    installationId: String?,
    circuitViewModel: CircuitViewModel,
    applianceViewModel: ApplianceViewModel,
    onBack: () -> Unit,
    contentPadding: PaddingValues
) {
    val ownerId = LocalCurrentUserSession.current?.userId
    val circuits = circuitViewModel.circuits.collectAsState().value
    val appliance = applianceCatalog.find { it.applianceId == applianceId }

    if (appliance == null) { onBack(); return }

    // Même dégradé que LoginScreen
    val bgTop    = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    val bgBottom = MaterialTheme.colorScheme.background

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
            // ── Header image + bouton retour ──────────────────────
            ItemDetailHeader(
                id = appliance.type.imageResId,
                label = stringResource(R.string.home_add_appliance_title),
                onBack = onBack
            )

            // ── Surface arrondie qui remonte sur le header ────────
            Surface(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-28).dp)   // chevauchement sur le header
            ) {
                ApplianceForm(
                    item = appliance,
                    circuits = circuits,
                    onSaveAppliance = { newAppliance ->
                        applianceViewModel.insert(
                            ownerId ?: "",
                            installationId ?: "",
                            newAppliance
                        )
                        onBack()
                    }
                )
            }

            Spacer(
                modifier = Modifier.height(
                    contentPadding.calculateBottomPadding() + 52.dp
                )
            )
        }
    }
}