package com.syme.ui.screen.home

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
    onNavigateToInstallationDetail: (Installation) -> Unit,
    onNavigateToUserInstallationDetail: (Installation) -> Unit = {}
) {

    val currentUser = LocalCurrentUserSession.current

    var selectedType by remember { mutableStateOf<InstallationType?>(null) }

    val state by installationViewModel.state.collectAsState()

    // 🔭 Observation Firestore
    LaunchedEffect(currentUser?.userId) {
        val userId = currentUser?.userId
        if (!userId.isNullOrBlank()) {
            installationViewModel.observe(userId)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item {
            Title(stringResource(R.string.home_welcome_message).format(currentUser?.firstName ?: ""))
        }

        item { Banner() }

        item {
            Title(
                title = stringResource(R.string.home_installation_type),
                fontSize = 18
            )
        }

        item {
            InstallationTypeFilterByType(
                selectedType = selectedType,
                onTypeSelected = { selectedType = it }
            )
        }

        // 🧱 TITRE SYSTEM (catalog)
        item {
            Title(
                title = stringResource(R.string.installation_add_title),
                fontSize = 18
            )
        }

        // 🧱 SYSTEM (filtré) on y touche pas
        item {
            val filteredCatalog =
                if (selectedType == null) installationCatalog
                else installationCatalog.filter { it.type == selectedType }

            InstallationRow(
                items = filteredCatalog,
                onClick = onNavigateToInstallationDetail
            )
        }

        item { HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .padding(vertical = 16.dp)
        ) }

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
                        onClick = onNavigateToUserInstallationDetail
                    )
                }

                is UiState.Error -> {
                    Text(stringResource(R.string.installation_error_loading_installations))
                }

                else -> {
                    UserInstallationsList(
                        items = emptyList(),
                        onClick = {}
                    )
                }
            }
        }
    }
}
