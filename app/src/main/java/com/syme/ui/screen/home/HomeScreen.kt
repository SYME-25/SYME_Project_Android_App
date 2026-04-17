package com.syme.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.R
import com.syme.domain.mapper.labelResId
import com.syme.domain.model.Installation
import com.syme.domain.model.enumeration.InstallationType
import com.syme.domain.state.UiState
import com.syme.ui.component.animation.banner.Banner
import com.syme.ui.component.card.InstallationRow
import com.syme.ui.component.compositionlocal.LocalCurrentUserSession
import com.syme.ui.component.filter.FilterSection
import com.syme.ui.component.text.SectionHeader
import com.syme.ui.component.text.Title
import com.syme.ui.screen.installation.components.UserInstallationsList
import com.syme.ui.viewmodel.InstallationViewModel
import com.syme.utils.TimeUtils
import com.syme.utils.installationCatalog

@Composable
fun HomeScreen(
    installationViewModel: InstallationViewModel,
    onNavigateToInstallationDetail: (Installation) -> Unit,
    onNavigateToUserInstallationDetail: (Installation) -> Unit = {},
    contentPadding : PaddingValues
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
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Title ────────────────────────────────────────────────────────────
        item {
            Title(
                title = stringResource(R.string.bottom_title_home),
                padding = 16
            )
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        // ── Greeting ─────────────────────────────────────────────────────────
        item {
            HomeGreeting(
                name = currentUser?.firstName ?: "",
                initials = buildInitials(
                    currentUser?.firstName,
                    currentUser?.lastName
                )
            )
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        // ── Banner ───────────────────────────────────────────────────────────
        item {
            Banner(id = R.raw.concept_smart_home)
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // ── Section divider ──────────────────────────────────────────────────
        item {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // ── Filter ───────────────────────────────────────────────────────────
        item {
            FilterSection(
                title = stringResource(R.string.home_installation_type),
                items = InstallationType.entries,
                selectedItem = selectedType,
                onItemSelected = { selectedType = it },
                itemLabel = { stringResource(it.labelResId) },
                allLabel = stringResource(R.string.home_installation_filter_all)
            )
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
            SectionHeader(
                title = stringResource(R.string.installation_add_title),
                onAddClick = { },
                isButton = false
            )
        }

        // ── Catalog section ──────────────────────────────────────────────────
        item {
            val filteredCatalog = if (selectedType == null) installationCatalog
            else installationCatalog.filter { it.type == selectedType }

            InstallationRow(
                items = filteredCatalog,
                onClick = onNavigateToInstallationDetail
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // ── User installations ───────────────────────────────────────────────
        item {
            SectionHeader(
                title = stringResource(R.string.home_your_installations),
                onAddClick = { },
                isButton = false
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            when (state) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
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

        item {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(contentPadding.calculateBottomPadding() + 32.dp)
            )
        }
    }
}

// ── Greeting ──────────────────────────────────────────────────────────────────
@Composable
private fun HomeGreeting(
    name: String,
    initials: String
) {
    val displayName = name.ifBlank {
        stringResource(R.string.home_greeting_fallback)
    }
    val hour = TimeUtils.hour
    val greetingRes = when (hour) {
        in 5..11 -> R.string.home_greeting_morning
        in 12..17 -> R.string.home_greeting_afternoon
        else -> R.string.home_greeting_evening
    }
    val subtitleRes = when (hour) {
        in 5..11 -> R.string.home_greeting_subtitle_morning
        in 12..17 -> R.string.home_greeting_subtitle_afternoon
        else -> R.string.home_greeting_subtitle_evening
    }
    val greetingIcon = when (hour) {
        in 5..11 -> "☀️"
        in 12..17 -> "🌤"
        else -> "🌙"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // ── Top row : avatar + greeting label ────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar circle with initials
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials.ifBlank { "?" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    // Dynamic greeting label
                    Text(
                        text = "$greetingIcon  ${stringResource(greetingRes)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Name — large & bold
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 26.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Thin divider ─────────────────────────────────────────────────
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.10f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Subtitle / energy tip ─────────────────────────────────────────
            Text(
                text = stringResource(subtitleRes),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
        }
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
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            fontSize = 14.sp
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────
private fun buildInitials(firstName: String?, lastName: String?): String {
    val f = firstName?.firstOrNull()?.uppercaseChar() ?: ""
    val l = lastName?.firstOrNull()?.uppercaseChar() ?: ""
    return "$f$l"
}