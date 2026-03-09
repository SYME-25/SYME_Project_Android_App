package com.syme.ui.screen.bill

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.syme.R
import com.syme.domain.model.Invoice

// ─────────────────────────────────────────────────────────────────────────────
// LISTE DES FACTURES
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Affiche la liste scrollable des factures.
 */
@Composable
fun BillList(
    invoices: List<Invoice>,
    installations: List<String>,
    moneyUnit: String,
    onExportPdf: (Invoice) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        invoices.forEach { invoice ->
            BillItemCard(
                invoice = invoice,
                moneyUnit = moneyUnit,
                onExportPdf = { onExportPdf(invoice) }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ÉTAT VIDE
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Affichage quand il n'y a aucune facture disponible.
 */
@Composable
fun BillEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector        = Icons.Outlined.Receipt,
            contentDescription = null,
            modifier           = Modifier.size(56.dp),
            tint               = MaterialTheme.colorScheme.outline
        )
        Text(
            text      = stringResource(R.string.bill_empty_title),
            style     = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text      = stringResource(R.string.bill_empty_subtitle),
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// OVERLAY D'EXPORT
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Overlay semi-transparent affiché pendant la génération du PDF.
 */
@Composable
fun ExportLoadingOverlay() {
    Box(
        modifier           = Modifier.fillMaxSize(),
        contentAlignment   = Alignment.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text(
                    text  = stringResource(R.string.bill_export_generating),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
