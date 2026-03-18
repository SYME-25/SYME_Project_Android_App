package com.syme.ui.screen.bill

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.R
import com.syme.domain.model.Bill
import com.syme.domain.model.Installation
import com.syme.ui.component.card.BillCardAdaptive
import com.syme.ui.component.compositionlocal.LocalCurrentUserSession
import com.syme.ui.component.text.Title
import com.syme.ui.screen.consumption.InstallationFilterById
import com.syme.ui.snapshot.MessageType
import com.syme.ui.snapshot.globalMessageManager
import com.syme.ui.state.UiState
import com.syme.ui.viewmodel.BillViewModel
import com.syme.ui.viewmodel.InstallationViewModel

@Composable
fun BillScreen(
    installationViewModel: InstallationViewModel,
    billViewModel: BillViewModel,
    onFilterSelected: (String?) -> Unit = {}
) {

    val currentUser = LocalCurrentUserSession.current
    val userId = currentUser?.userId ?: ""

    val installationState by installationViewModel.state.collectAsState()
    val bills by billViewModel.bills.collectAsState()
    val context = LocalContext.current

    // 🔹 Liste des installations
    val installationsMap = remember(installationState) {
        when (installationState) {
            is UiState.Success ->
                (installationState as UiState.Success<List<Installation>>)
                    .data.associate { it.name to it.installationId }
            else -> emptyMap()
        }
    }

    val installationNames = installationsMap.keys.toList()

    var selectedInstallationId by remember {
        mutableStateOf<String?>(null)
    }

    var showExportDialog by remember { mutableStateOf(false) }
    var selectedBill by remember { mutableStateOf<Bill?>(null) }

    // 🔹 Observer installations
    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            installationViewModel.observe(userId)
        }
    }

    // 🔹 Définir installation par défaut
    LaunchedEffect(installationsMap) {
        if (selectedInstallationId == null && installationsMap.isNotEmpty()) {
            selectedInstallationId = installationsMap.values.first()
        }
    }

    // 🔹 Observer les factures
    LaunchedEffect(selectedInstallationId) {
        selectedInstallationId?.let {
            billViewModel.observeBills(userId, it)
        }
    }

    val currentBill = bills.firstOrNull()
    val historyBills = if (bills.size > 1) bills.drop(1) else emptyList()

    val billExportMsg = stringResource(R.string.bill_export)
    val billExportSuccessMsg = stringResource(R.string.bill_export_success)
    val billExportFailedMsg = stringResource(R.string.bill_export_failed)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item {
            Title(stringResource(R.string.bill_title))
        }

        item { BannerBill() }

        // 🔹 Filtre installation
        item {

            if (installationNames.isNotEmpty()) {

                InstallationFilterById(
                    title = stringResource(R.string.consumption_filter_by_installation),
                    installationIds = installationNames,
                    selectedInstallationId = installationsMap.entries
                        .firstOrNull { it.value == selectedInstallationId }?.key,
                    onInstallationSelected = { selectedName ->

                        selectedInstallationId = installationsMap[selectedName]

                        onFilterSelected(selectedInstallationId)
                    }
                )
            }
        }

        // Factures
        if (bills.isNotEmpty()) {
            if (currentBill != null) {

                item {
                    SectionLabel(stringResource(R.string.current_bill))
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        BillCardAdaptive(
                            bill = currentBill,
                            onBillClick = {
                                selectedBill = it
                                showExportDialog = true
                            }
                        )
                    }
                }
            }

            item {
                SectionLabel(stringResource(R.string.history_bills))
            }

            if (historyBills.isNotEmpty()) {

                items(historyBills.chunked(2)) { rowBills ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowBills.forEach { bill ->
                            BillCardAdaptive(
                                bill = bill,
                                onBillClick = {
                                    selectedBill = bill
                                    showExportDialog = true
                                }
                            )
                        }
                    }
                }

            } else {

                item {
                    NoHistoryPlaceholder()
                }

            }

        } else {
            item { NoBillsPlaceholder() }
        }
    }

    selectedBill?.let { bill ->

        if (showExportDialog) {

            BillExportDialog(
                bill = bill,
                onDismiss = { showExportDialog = false },
                onExportClick = { email ->

                    // Appel à l'export
                    billViewModel.exportBill(context, bill, email) { success ->
                        showExportDialog = false
                        if (success) {
                            globalMessageManager.showMessage(
                                item = billExportMsg,
                                type = MessageType.SUCCESS,
                                customText = billExportSuccessMsg
                            )
                        } else {
                            globalMessageManager.showMessage(
                                item = billExportMsg,
                                type = MessageType.ERROR,
                                customText = billExportFailedMsg
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun NoBillsPlaceholder(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // Icon
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            modifier = Modifier
                .size(80.dp)
                .padding(bottom = 16.dp)
        )

        // Title
        Text(
            text = stringResource(R.string.no_bills),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle / description
        Text(
            text = stringResource(R.string.no_bills_description),
            fontSize = 14.sp,
            maxLines = 2,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SectionLabel(text: String) {

    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun NoHistoryPlaceholder() {

    Text(
        text = stringResource(R.string.no_history_bills),
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    )
}