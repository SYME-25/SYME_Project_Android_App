package com.syme.ui.screen.bill

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syme.R
import com.syme.domain.model.BillingPeriod
import com.syme.domain.model.Invoice
import com.syme.domain.model.enumeration.InvoiceStatus
import java.text.SimpleDateFormat
import java.util.*

/**
 * Carte Material 3 représentant une facture dans la liste.
 *
 * Affiche : numéro, période, montant, statut et bouton d'export PDF.
 */
@Composable
fun BillItemCard(
    invoice: Invoice,
    moneyUnit: String,
    onExportPdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── En-tête : numéro + statut ─────────────────────────────────
            Row(
                modifier       = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text  = stringResource(R.string.invoice_number_prefix) + invoice.invoiceId,
                    style = MaterialTheme.typography.titleSmall
                )
                InvoiceStatusChip(status = invoice.status)
            }

            Spacer(Modifier.height(8.dp))

            // ── Période de facturation ────────────────────────────────────
            Text(
                text  = "${df.format(Date(invoice.billingPeriod.periodStart))} " +
                        "→ ${df.format(Date(invoice.billingPeriod.periodEnd))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            // ── Montant + bouton export ───────────────────────────────────
            Row(
                modifier       = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text  = "%.0f $moneyUnit".format(invoice.totalAmount),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                FilledTonalIconButton(onClick = onExportPdf) {
                    Icon(
                        imageVector        = Icons.Outlined.PictureAsPdf,
                        contentDescription = stringResource(R.string.invoice_export_pdf)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CHIP STATUT
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InvoiceStatusChip(status: InvoiceStatus) {
    val (label, container, content) = when (status) {
        InvoiceStatus.PAID      -> Triple(
            stringResource(R.string.invoice_status_paid),
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        InvoiceStatus.OVERDUE   -> Triple(
            stringResource(R.string.invoice_status_overdue),
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        InvoiceStatus.ISSUED    -> Triple(
            stringResource(R.string.invoice_status_issued),
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        InvoiceStatus.DRAFT     -> Triple(
            stringResource(R.string.invoice_status_draft),
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        InvoiceStatus.CANCELLED -> Triple(
            stringResource(R.string.invoice_status_cancelled),
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = container,
        contentColor = content
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun BillItemCardPreview() {
    val fakeInvoice = Invoice(
        invoiceId       = "INV-2024-001",
        ownerId         = "user_123",
        installationId  = "inst_456",
        status          = InvoiceStatus.PAID,
        totalAmount     = 142500.0,
        billingPeriod   = BillingPeriod(
            periodStart = 1704067200000L, // 01/01/2024
            periodEnd = 1706745600000L  // 31/01/2024
        ),
        demandLines     = emptyList()
    )

    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            BillItemCard(
                invoice     = fakeInvoice,
                moneyUnit   = "FCFA",
                onExportPdf = {}
            )
        }
    }
}