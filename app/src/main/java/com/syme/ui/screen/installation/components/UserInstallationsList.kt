package com.syme.ui.screen.installation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syme.R
import com.syme.domain.model.Installation
import com.syme.ui.component.animation.Animation
import com.syme.ui.component.card.InstallationRow
import com.syme.ui.component.text.SectionLabel

@Composable
fun UserInstallationsList(
    items: List<Installation>,
    onClick: (Installation) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        SectionLabel(label = stringResource(R.string.home_your_installations))

        if (items.isEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Animation(R.raw.blue_house)
                Text(
                    text = stringResource(R.string.home_no_installations_found),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            InstallationRow(
                items = items,
                onClick = onClick
            )
        }
    }
}