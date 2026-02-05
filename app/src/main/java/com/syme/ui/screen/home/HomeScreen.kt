package com.syme.ui.screen.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.syme.R
import com.syme.domain.model.Installation
import com.syme.domain.model.enumeration.InstallationType
import com.syme.ui.component.card.InstallationRow
import com.syme.ui.component.compositionlocal.LocalCurrentUserSession
import com.syme.ui.component.text.Title
import com.syme.ui.screen.installation.InstallationTypeFilterByType
import com.syme.ui.screen.installation.UserInstallationsList
import com.syme.ui.state.UiState
import com.syme.ui.viewmodel.InstallationViewModel
import com.syme.utils.installationCatalog

@Composable
fun HomeScreen(
    installationViewModel: InstallationViewModel, // 👈 passé depuis le NavGraph
    ownerId: String,                               // 👈 id user courant
    onNavigateToInstallationDetail: (Installation) -> Unit = {}
) {

    val currentUser = LocalCurrentUserSession.current

    var selectedType by remember { mutableStateOf<InstallationType?>(null) }

    val state by installationViewModel.state.collectAsState()

    // 🔭 Observation Firestore
    LaunchedEffect(ownerId) {
        installationViewModel.observe(ownerId)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item {
            Title(stringResource(R.string.home_welcome_message).format(currentUser?.firstName))
        }

        item { Banner() }

        item {
            InstallationTypeFilterByType(
                title = stringResource(R.string.home_installation_type),
                selectedType = selectedType,
                onTypeSelected = { selectedType = it }
            )
        }

        // 🧱 SYSTEM (filtré)
        item {
            val filteredCatalog =
                if (selectedType == null) installationCatalog
                else installationCatalog.filter { it.type == selectedType }

            InstallationRow(
                items = filteredCatalog,
                onClick = onNavigateToInstallationDetail
            )
        }

        // 👤 USER (branché Firestore)
        item {
            when (state) {
                is UiState.Loading -> {
                    CircularProgressIndicator()
                }

                is UiState.Success -> {
                    val userInstallations =
                        (state as UiState.Success<List<Installation>>).data

                    val filteredUserInstallations =
                        if (selectedType == null) userInstallations
                        else userInstallations.filter { it.type == selectedType }

                    UserInstallationsList(
                        items = filteredUserInstallations,
                        onClick = onNavigateToInstallationDetail
                    )
                }

                is UiState.Error -> {
                    Text("Erreur de chargement des installations")
                }

                else -> {
                    UserInstallationsList(items = emptyList(), onClick = {})
                }
            }
        }
    }
}
