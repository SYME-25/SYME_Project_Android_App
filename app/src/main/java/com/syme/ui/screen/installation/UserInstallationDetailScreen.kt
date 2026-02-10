package com.syme.ui.screen.installation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.syme.R
import com.syme.domain.mapper.allowedApplianceTypes
import com.syme.domain.mapper.imageResId
import com.syme.domain.model.Appliance
import com.syme.domain.model.enumeration.ApplianceHeatType
import com.syme.domain.model.enumeration.ApplianceType
import com.syme.domain.model.enumeration.InstallationType
import com.syme.ui.component.card.ApplianceRow
import com.syme.ui.component.compositionlocal.LocalCurrentUserSession
import com.syme.ui.component.text.Title
import com.syme.ui.screen.appliance.ApplianceHeatTypeFilter
import com.syme.ui.screen.appliance.ApplianceFilterByInstallationType
import com.syme.ui.screen.appliance.UserAppliancesList
import com.syme.ui.state.UiState
import com.syme.ui.viewmodel.ApplianceViewModel
import com.syme.ui.viewmodel.InstallationViewModel
import com.syme.utils.applianceCatalog

@Composable
fun UserInstallationDetailScreen(
    installationId: String,
    installationViewModel: InstallationViewModel,
    applianceViewModel: ApplianceViewModel,
    onApplianceClick: (Appliance) -> Unit = {}
) {
    val currentUser = LocalCurrentUserSession.current

    val selectedInstallation by installationViewModel.selected.collectAsState()
    val applianceState by applianceViewModel.state.collectAsState()

    var selectedInstallationType by remember { mutableStateOf<InstallationType?>(null) }
    var selectedHeatType by remember { mutableStateOf<ApplianceHeatType?>(null) }

    // 🔭 Charger installation + observer appliances
    LaunchedEffect(installationId, currentUser?.userId) {
        val userId = currentUser?.userId
        if (!userId.isNullOrBlank()) {
            installationViewModel.getById(userId, installationId)
            applianceViewModel.observe(userId, installationId)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 🏷 Nom installation
        item {
            Title(title = selectedInstallation?.name ?: "", fontSize = 30)
        }

        // 🖼 Banner
        item {
            BannerUserInstallation(
                id = selectedInstallation?.type?.imageResId ?: 0
            )
        }

        item {
            Title(
                title = stringResource(R.string.appliance_filter_type),
                fontSize = 18
            )
        }

        // 🎛 Filtre par TYPE D'INSTALLATION
        item {
            InstallationTypeFilterByType(
                selectedType = selectedInstallationType,
                onTypeSelected = { selectedInstallationType = it }
            )
        }

        // 🌡 HeatType filter
        item {
            ApplianceHeatTypeFilter(
                title = stringResource(R.string.appliance_filter_heat_type),
                selectedHeatType = selectedHeatType,
                onHeatTypeSelected = { selectedHeatType = it }
            )
        }

        // 🧱 TITRE SYSTEM (catalog)
        item {
            Title(
                title = stringResource(R.string.appliance_add_title),
                fontSize = 18
            )
        }

        // 🧱 SYSTEM (catalog)
        item {
            val filteredCatalog = applianceCatalog
                .let { list ->
                    if (selectedInstallationType != null)
                        list.filter { it.type in selectedInstallationType!!.allowedApplianceTypes }
                    else list
                }
                .let { list ->
                    if (selectedHeatType != null) list.filter { it.heatType == selectedHeatType } else list
                }

            ApplianceRow(
                items = filteredCatalog,
                onClick = onApplianceClick
            )
        }

        // ───────── Divider
        item {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .padding(vertical = 16.dp)
            )
        }

        // 👤 USER (Firestore)
        item {
            when (applianceState) {
                is UiState.Loading -> CircularProgressIndicator()

                is UiState.Success -> {
                    val userAppliances = (applianceState as UiState.Success<List<Appliance>>).data

                    val filteredUserAppliances = userAppliances
                        .let { list ->
                            if (selectedInstallationType != null)
                                list.filter { it.type in selectedInstallationType!!.allowedApplianceTypes }
                            else list
                        }
                        .let { list ->
                            if (selectedHeatType != null) list.filter { it.heatType == selectedHeatType } else list
                        }

                    UserAppliancesList(
                        items = filteredUserAppliances,
                        onClick = onApplianceClick
                    )
                }

                is UiState.Error -> Text(stringResource(R.string.installation_error_loading_installations))

                else -> UserAppliancesList(items = emptyList(), onClick = {})
            }
        }
    }
}
