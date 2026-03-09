package com.syme.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syme.R
import com.syme.data.remote.repository.InvoiceRepository
import com.syme.data.remote.repository.MeterRepository
import com.syme.domain.model.ExportState
import com.syme.domain.model.Installation
import com.syme.domain.model.Invoice
import com.syme.domain.usecase.AutoBillingOrchestrator
import com.syme.ui.screen.bill.pdf.InvoicePdfGenerator
import com.syme.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class BillViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val meterRepository: MeterRepository,
    private val autoBillingOrchestrator: AutoBillingOrchestrator
) : ViewModel() {

    // ── Invoice list (current + history merged) ──────────────────────────────
    private val _invoices = MutableStateFlow<UiState<List<Invoice>>>(UiState.Loading)
    val invoices: StateFlow<UiState<List<Invoice>>> = _invoices.asStateFlow()

    // ── Current live invoice ─────────────────────────────────────────────────
    private val _currentInvoice = MutableStateFlow<Invoice?>(null)
    val currentInvoice: StateFlow<Invoice?> = _currentInvoice.asStateFlow()

    // ── Export PDF state ─────────────────────────────────────────────────────
    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    private var observeJob: Job? = null
    private var billingJob: Job? = null

    // ─────────────────────────────────────────────────────────────────────────
    // OBSERVE INVOICES (UI display)
    // ─────────────────────────────────────────────────────────────────────────

    fun observeInvoices(ownerId: String, installationId: String) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _invoices.value = UiState.Loading
            try {
                invoiceRepository.observeAll(ownerId, installationId)
                    .collect { list -> _invoices.value = UiState.Success(list) }
            } catch (e: Exception) {
                _invoices.value = UiState.Error(e.message ?: "Erreur de chargement")
            }
        }

        // Also observe just the current invoice separately
        viewModelScope.launch {
            invoiceRepository.observeCurrent(ownerId, installationId)
                .collect { _currentInvoice.value = it }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AUTO-BILLING — triggered on each new aggregated measurement
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Starts watching aggregated measurements for a given installation.
     * Each new measurement triggers [AutoBillingOrchestrator.run], which
     * recomputes and upserts the current invoice in Firestore.
     */
    fun startAutoBilling(
        ownerId: String,
        installation: Installation
    ) {
        billingJob?.cancel()
        billingJob = viewModelScope.launch {
            meterRepository
                .observeAggregatedMeasurementsFromFirestore(ownerId, installation.installationId)
                .distinctUntilChanged() // Only recompute when measurements actually change
                .collect {
                    // Run on IO — never block the main thread
                    withContext(Dispatchers.IO) {
                        runCatching {
                            autoBillingOrchestrator.run(ownerId, installation)
                        }
                        // Errors are silently swallowed here to avoid crashing the observer.
                        // Add logging/monitoring as needed.
                    }
                }
        }
    }

    fun stopAutoBilling() {
        billingJob?.cancel()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EXPORT PDF
    // ─────────────────────────────────────────────────────────────────────────

    fun exportInvoicePdf(
        context: Context,
        invoice: Invoice,
        installation: Installation?,
        companyName: String    = "SYME Energy",
        companyAddress: String = "Pointe-Noire, République du Congo",
        companyEmail: String   = "contact@syme.energy",
        moneyUnit: String      = "FCFA"
    ) {
        viewModelScope.launch {
            _exportState.value = ExportState.Loading
            try {
                val pdfFile = withContext(Dispatchers.IO) {
                    InvoicePdfGenerator.generate(
                        context        = context,
                        invoice        = invoice,
                        installation   = installation,
                        companyName    = companyName,
                        companyAddress = companyAddress,
                        companyEmail   = companyEmail,
                        moneyUnit      = moneyUnit
                    )
                }
                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    pdfFile
                )
                _exportState.value = ExportState.Success(
                    intent = Intent.createChooser(
                        buildEmailIntent(context, uri, invoice),
                        context.getString(R.string.invoice_email_chooser)
                    ),
                    file = pdfFile
                )
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(
                    e.message ?: "Erreur lors de la génération du PDF"
                )
            }
        }
    }

    fun resetExportState() { _exportState.value = ExportState.Idle }

    fun clearPdfCache(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                File(context.cacheDir, "invoices").listFiles()?.forEach { it.delete() }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    private fun buildEmailIntent(context: Context, uri: Uri, invoice: Invoice) =
        Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(
                Intent.EXTRA_SUBJECT,
                context.getString(R.string.invoice_email_subject, invoice.invoiceId)
            )
            putExtra(
                Intent.EXTRA_TEXT,
                context.getString(
                    R.string.invoice_email_body,
                    invoice.billingPeriod.periodStart.toString(),
                    invoice.billingPeriod.periodEnd.toString()
                )
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

    override fun onCleared() {
        super.onCleared()
        stopAutoBilling()
    }
}
