package com.syme.ui.screen.bill

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.DateRange
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
import com.syme.domain.state.UiState
import com.syme.ui.component.animation.banner.Banner
import com.syme.ui.component.card.BillCardAdaptive
import com.syme.ui.component.compositionlocal.LocalCurrentUserSession
import com.syme.ui.component.dialog.BillExportDialog
import com.syme.ui.component.filter.FilterSection
import com.syme.ui.component.state.EmptyStatePlaceholder
import com.syme.ui.component.text.Title
import com.syme.ui.snapshot.MessageType
import com.syme.ui.snapshot.globalMessageManager
import com.syme.ui.viewmodel.BillViewModel
import com.syme.ui.viewmodel.InstallationViewModel

@Composable
fun BillScreen(
    installationViewModel: InstallationViewModel,
    billViewModel: BillViewModel,
    onFilterSelected: (String?) -> Unit = {},
    contentPadding : PaddingValues
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

    val now = System.currentTimeMillis()

    val sortedBills = bills.sortedByDescending { it.periodEnd }

    val currentBill = sortedBills.firstOrNull { it.periodEnd >= now }
        ?: sortedBills.firstOrNull() // fallback : la plus récente si toutes sont passées

    val historyBills = sortedBills
        .filter { it.billId != currentBill?.billId && it.periodEnd < now }

    val billExportMsg = stringResource(R.string.bill_export)
    val billExportSuccessMsg = stringResource(R.string.bill_export_success)
    val billExportFailedMsg = stringResource(R.string.bill_export_failed)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item {
            Title(
                stringResource(R.string.bill_title),
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        item { Banner(id = R.raw.money) }

        // 🔹 Filtre installation
        item {

            if (installationNames.isNotEmpty()) {

                FilterSection(
                    title = stringResource(R.string.consumption_filter_by_installation),
                    items = installationNames,
                    selectedItem = installationsMap.entries
                        .firstOrNull { it.value == selectedInstallationId }?.key,
                    onItemSelected = { selectedName ->
                        selectedInstallationId = installationsMap[selectedName]
                        onFilterSelected(selectedInstallationId)
                    },
                    itemLabel = { name ->
                        if (name.length > 12) name.take(12) + "…" else name
                    },
                    showAll = false // 👈 important
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
            item {
                EmptyStatePlaceholder(
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    title = stringResource(R.string.no_bills),
                    description = stringResource(R.string.no_bills_description),
                )
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