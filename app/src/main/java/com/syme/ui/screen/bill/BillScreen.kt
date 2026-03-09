package com.syme.ui.screen.bill

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.syme.R
import com.syme.domain.model.ExportState
import com.syme.domain.model.Installation
import com.syme.ui.component.compositionlocal.LocalCurrentUserSession
import com.syme.ui.component.text.Title
import com.syme.ui.screen.consumption.InstallationFilterById
import com.syme.ui.state.UiState
import com.syme.ui.viewmodel.BillViewModel
import com.syme.ui.viewmodel.InstallationViewModel
import com.syme.ui.viewmodel.MeterViewModel
import com.syme.ui.viewmodel.ConsumptionViewModel

@Composable
fun BillScreen(
    billViewModel: BillViewModel,
    installationViewModel: InstallationViewModel,
    meterViewModel: MeterViewModel,
    consumptionViewModel: ConsumptionViewModel,
    onFilterSelected: (String?) -> Unit = {},
    moneyUnit: String  = "FCFA",
    companyName: String    = "SYME Energy",
    companyAddress: String = "Pointe-Noire, République du Congo",
    companyEmail: String   = "contact@syme.energy"
) {
    val context     = LocalContext.current
    val currentUser = LocalCurrentUserSession.current
    val userId      = currentUser?.userId ?: ""

    // ── Installations ────────────────────────────────────────────────────────
    val installationState by installationViewModel.state.collectAsState()

    val installationsMap = remember(installationState) {
        when (installationState) {
            is UiState.Success ->
                (installationState as UiState.Success<List<Installation>>)
                    .data.associate { it.name to it }       // name → Installation object
            else -> emptyMap()
        }
    }
    val installationNames = installationsMap.keys.toList()

    var selectedInstallation by remember { mutableStateOf<Installation?>(null) }

    // ── Invoice / export states ───────────────────────────────────────────────
    val invoicesState   by billViewModel.invoices.collectAsState()
    val currentInvoice  by billViewModel.currentInvoice.collectAsState()
    val exportState     by billViewModel.exportState.collectAsState()
    val snackbarState   = remember { SnackbarHostState() }

    val emailLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { billViewModel.resetExportState() }

    // ── Effects ──────────────────────────────────────────────────────────────

    LaunchedEffect(exportState) {
        when (val s = exportState) {
            is ExportState.Success -> emailLauncher.launch(s.intent)
            is ExportState.Error   -> snackbarState.showSnackbar(s.message)
            else -> Unit
        }
    }

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) installationViewModel.observe(userId)
    }

    // Default selection once installations load
    LaunchedEffect(installationsMap) {
        if (selectedInstallation == null && installationsMap.isNotEmpty()) {
            selectedInstallation = installationsMap.values.first()
        }
    }

    // Observe invoices + start auto-billing whenever selected installation changes
    LaunchedEffect(userId, selectedInstallation) {
        val installation = selectedInstallation ?: return@LaunchedEffect
        if (userId.isBlank()) return@LaunchedEffect

        billViewModel.observeInvoices(userId, installation.installationId)
        billViewModel.startAutoBilling(userId, installation)
    }

    // Stop auto-billing when screen leaves composition
    DisposableEffect(Unit) {
        onDispose { billViewModel.stopAutoBilling() }
    }

    // ── UI ───────────────────────────────────────────────────────────────────
    val invoicesList = (invoicesState as? UiState.Success)?.data ?: emptyList()

    Scaffold(snackbarHost = { SnackbarHost(snackbarState) }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Title(stringResource(R.string.invoice_header_title)) }
            item { BannerBill() }

            // ── Installation filter ──────────────────────────────────────────
            item {
                if (installationNames.isNotEmpty()) {
                    InstallationFilterById(
                        title = stringResource(R.string.consumption_filter_by_installation),
                        installationIds = installationNames,
                        selectedInstallationId = selectedInstallation?.name,
                        onInstallationSelected = { name ->
                            selectedInstallation = installationsMap[name]
                            onFilterSelected(selectedInstallation?.installationId)
                        }
                    )
                }
            }

            // ── Current invoice penalty lines (if any) ───────────────────────
            currentInvoice?.penaltyLines
                ?.takeIf { it.isNotEmpty() }
                ?.let { penalties ->
                    item {
                        Text(
                            text  = stringResource(R.string.penalty_section_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        )
                    }
                    penalties.forEach { penalty ->
                        item(key = "${penalty.gapStart}-${penalty.gapEnd}") {
                            PenaltyCard(
                                penaltyLine = penalty,
                                moneyUnit   = moneyUnit
                            )
                        }
                    }
                }

            // ── Invoice list (current + history) ─────────────────────────────
            item {
                BillScreenContent(
                    invoices    = invoicesList,
                    installations = installationNames,
                    moneyUnit   = moneyUnit,
                    isLoading   = invoicesState is UiState.Loading,
                    isExporting = exportState is ExportState.Loading,
                    onExportPdf = { invoice ->
                        val matched = installationsMap.values
                            .firstOrNull { it.installationId == invoice.installationId }
                        billViewModel.exportInvoicePdf(
                            context        = context,
                            invoice        = invoice,
                            installation   = matched,
                            companyName    = companyName,
                            companyAddress = companyAddress,
                            companyEmail   = companyEmail,
                            moneyUnit      = moneyUnit
                        )
                    }
                )
            }
        }
    }
}
