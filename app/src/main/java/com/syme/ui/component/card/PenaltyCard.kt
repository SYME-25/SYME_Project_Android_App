package com.syme.ui.screen.bill

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syme.R
import com.syme.domain.model.PenaltyLine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Card displaying a single [PenaltyLine] — an uncovered gap in the subscription
 * that triggered an out-of-contract penalty.
 *
 * Shows:
 *  - Warning icon + "Hors abonnement" label
 *  - Gap interval (start → end)
 *  - Duration in hours
 *  - Estimated energy (kWh)
 *  - Penalty cost
 */
@Composable
fun PenaltyCard(
    penaltyLine: PenaltyLine,
    moneyUnit: String = "FCFA",
    modifier: Modifier = Modifier
) {
    val dateFormatter = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector        = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.error,
                    modifier           = Modifier.size(20.dp)
                )
                Text(
                    text       = stringResource(R.string.penalty_label),
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.error
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f))

            // ── Period ────────────────────────────────────────────────────────
            PenaltyRow(
                label = stringResource(R.string.penalty_period_start),
                value = dateFormatter.format(Date(penaltyLine.gapStart))
            )
            PenaltyRow(
                label = stringResource(R.string.penalty_period_end),
                value = dateFormatter.format(Date(penaltyLine.gapEnd))
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── Metrics ───────────────────────────────────────────────────────
            PenaltyRow(
                label = stringResource(R.string.penalty_duration),
                value = stringResource(
                    R.string.penalty_duration_value,
                    "%.1f".format(penaltyLine.durationHours)
                )
            )
            PenaltyRow(
                label = stringResource(R.string.penalty_energy),
                value = stringResource(
                    R.string.penalty_energy_value,
                    "%.2f".format(penaltyLine.energyKwh)
                )
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── Cost ──────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text  = stringResource(R.string.penalty_cost),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text       = "${penaltyLine.cost.roundToInt()} $moneyUnit",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Internal helper row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PenaltyRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text  = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}
