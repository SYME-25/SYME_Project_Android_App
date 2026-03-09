package com.syme.ui.screen.bill

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syme.domain.model.Invoice
import com.syme.R

/**
 * Composable pur (stateless) pour l'écran des factures.
 *
 * Reçoit uniquement des données et des callbacks — aucune dépendance
 * sur le ViewModel ou le contexte Android. Facile à prévisualiser
 * et à tester en isolation.
 *
 * @param invoices      Liste des factures à afficher.
 * @param installations Noms des installations pour le filtre.
 * @param moneyUnit     Unité monétaire (ex : "FCFA").
 * @param isLoading     Affiche un indicateur de chargement si true.
 * @param isExporting   Affiche un overlay de génération PDF si true.
 * @param onExportPdf   Callback déclenché quand l'utilisateur demande l'export.
 */
@Composable
fun BillScreenContent(
    invoices: List<Invoice>,
    installations: List<String>,
    moneyUnit: String,
    isLoading: Boolean   = false,
    isExporting: Boolean = false,
    onExportPdf: (Invoice) -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                    .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.bill_empty_subtitle),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            invoices.isEmpty() -> {
                BillEmptyState(modifier = Modifier.align(Alignment.Center))
            }
            else -> {
                BillList(
                    invoices      = invoices,
                    installations = installations,
                    moneyUnit     = moneyUnit,
                    onExportPdf   = onExportPdf
                )
            }
        }

        // Overlay pendant la génération du PDF
        if (isExporting) {
            ExportLoadingOverlay()
        }
    }
}
