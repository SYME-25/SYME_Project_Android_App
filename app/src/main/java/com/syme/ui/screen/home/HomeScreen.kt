package com.syme.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.R
import com.syme.domain.model.Installation
import com.syme.domain.model.enumeration.InstallationType
import com.syme.domain.state.UiState
import com.syme.ui.component.animation.banner.Banner
import com.syme.ui.component.card.InstallationRow
import com.syme.ui.component.compositionlocal.LocalCurrentUserSession
import com.syme.ui.component.text.Title
import com.syme.ui.screen.installation.components.InstallationTypeFilterByType
import com.syme.ui.screen.installation.components.UserInstallationsList
import com.syme.ui.viewmodel.InstallationViewModel
import com.syme.utils.installationCatalog

@Composable
fun HomeScreen(
    installationViewModel: InstallationViewModel,
    onNavigateToInstallationDetail: (Installation) -> Unit,
    onNavigateToUserInstallationDetail: (Installation) -> Unit = {}
) {
    val currentUser = LocalCurrentUserSession.current
    var selectedType by remember { mutableStateOf<InstallationType?>(null) }
    val state by installationViewModel.state.collectAsState()

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
            Title(
                title = stringResource(R.string.bottom_title_home),
                padding = 16
            )
        }

        // ── Greeting ────────────────────────────────────────────────────────
        item {
            HomeGreeting(name = currentUser?.firstName ?: "")
        }

        // ── Banner ──────────────────────────────────────────────────────────
        item {
            Banner(id = R.raw.concept_smart_home)
        }

        // ── Section divider ─────────────────────────────────────────────────
        item {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
            )
        }

        // ── Filter ──────────────────────────────────────────────────────────
        item {
            InstallationTypeFilterByType(
                title = stringResource(R.string.home_installation_type),
                selectedType = selectedType,
                onTypeSelected = { selectedType = it }
            )
        }

        // ── Catalog section ─────────────────────────────────────────────────
        item {
            val filteredCatalog = if (selectedType == null) installationCatalog
            else installationCatalog.filter { it.type == selectedType }
            InstallationRow(
                items = filteredCatalog,
                onClick = onNavigateToInstallationDetail
            )
        }

        item {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
            )
        }

        // ── User installations ───────────────────────────────────────────────
        item {
            when (state) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                    }
                }
                is UiState.Success -> {
                    val userInstallations =
                        (state as UiState.Success<List<Installation>>).data
                    val filtered = if (selectedType == null) userInstallations
                    else userInstallations.filter { it.type == selectedType }
                    UserInstallationsList(
                        items = filtered,
                        onClick = onNavigateToUserInstallationDetail
                    )
                }
                is UiState.Error -> {
                    HomeErrorState(
                        message = stringResource(R.string.installation_error_loading_installations)
                    )
                }
                else -> {
                    UserInstallationsList(items = emptyList(), onClick = {})
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

// ── Greeting ──────────────────────────────────────────────────────────────────
@Composable
private fun HomeGreeting(name: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Text(
            text = stringResource(R.string.home_greeting_hello),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            letterSpacing = 0.5.sp
        )
        Text(
            text = name.ifBlank { stringResource(R.string.home_greeting_fallback) },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

// ── Error state ───────────────────────────────────────────────────────────────
@Composable
private fun HomeErrorState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}