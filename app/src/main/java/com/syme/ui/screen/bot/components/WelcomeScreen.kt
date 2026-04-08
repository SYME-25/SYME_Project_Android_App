package com.syme.ui.screen.bot.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.syme.R
import com.syme.ui.component.animation.banner.Banner
import com.syme.ui.component.compositionlocal.LocalCurrentUserSession

@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    onSuggest: (String) -> Unit
) {
    val user = LocalCurrentUserSession.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center, // ← centre verticalement tout le contenu
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Welcome text ────────────────────────────────
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = String.format(stringResource(R.string.user_firstname), user?.firstName),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = stringResource(R.string.bot_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Bot animation ───────────────────────────────
        Banner(id = R.raw.syme_bot)

        Spacer(modifier = Modifier.height(24.dp))

        // ── Suggestion chips ───────────────────────────
        val suggestionChips = stringArrayResource(R.array.bot_suggestions)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.suggestions_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestionChips.forEach { suggestion ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.clickable { onSuggest(suggestion) }
                    ) {
                        Text(
                            text = suggestion,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Warning / disclaimer ────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.bot_warning_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = stringResource(R.string.bot_warning_consent),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.bot_warning_accuracy),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}