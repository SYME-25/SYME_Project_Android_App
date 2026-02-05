package com.syme.ui.screen.consumption

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.syme.R
import com.syme.domain.mapper.MeasurementConverter
import com.syme.domain.model.Consumption
import com.syme.domain.model.Installation
import com.syme.domain.model.Measurement
import com.syme.domain.model.enumeration.PeriodFilter
import com.syme.ui.alerts.AlertManager
import com.syme.ui.component.actionbutton.AppTextButton
import com.syme.ui.component.card.ConsumptionColumn
import com.syme.ui.component.chart.ConsumptionInjectionBarChart
import com.syme.ui.component.compositionlocal.LocalCurrentUserSession
import com.syme.ui.component.text.Title
import com.syme.ui.snapshot.GlobalMessageSnapshot
import com.syme.ui.snapshot.MessageAction
import com.syme.ui.snapshot.MessageType
import com.syme.ui.snapshot.globalMessageManager
import com.syme.ui.state.UiState
import com.syme.ui.viewmodel.ConsumptionViewModel
import com.syme.ui.viewmodel.InstallationViewModel
import com.syme.ui.viewmodel.MeasurementViewModel
import com.syme.utils.generateId
import java.time.LocalDate
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumptionScreen(
    installationViewModel: InstallationViewModel,
    consumptionViewModel: ConsumptionViewModel,
    measurementViewModel: MeasurementViewModel,
    onFilterSelected: (String?) -> Unit = {}
) {

    val currentUser = LocalCurrentUserSession.current
    val userId = currentUser?.userId ?: ""

    val installationState by installationViewModel.state.collectAsState()

// Extraire la liste des installationIds
    val installationIds = remember(installationState) {
        when (installationState) {
            is UiState.Success -> (installationState as UiState.Success<List<Installation>>).data.map { it.installationId }
            else -> emptyList()
        }
    }

    var selectedInstallationId by remember { mutableStateOf<String?>(null) }
    var selectedPeriod by remember { mutableStateOf(PeriodFilter.DAY) }
    var currentDate by remember { mutableStateOf(LocalDate.now()) }

    var showFormDialog by remember { mutableStateOf(false) }

    // 🔹 Observed states from ViewModels
    val consumptions by consumptionViewModel.consumptions.collectAsState()
    val measurements by measurementViewModel.measurements.collectAsState()

    // 🔹 Filtered list of consumptions for UI
    val allConsumptions = remember(consumptions) { consumptions.toMutableStateList() }

    // 🔹 Filter measurements based on installation and period
    val filteredMeasurements by remember(
        measurements, selectedInstallationId, selectedPeriod, currentDate
    ) {
        derivedStateOf {
            val byInstallation = if (selectedInstallationId != null) {
                measurements.filter { it.installationId == selectedInstallationId }
            } else measurements

            MeasurementConverter.filterMeasurementsByPeriod(
                byInstallation, selectedPeriod, currentDate
            )
        }
    }

    // 🔹 Convert to chart bars
    val consumptionBars by remember(filteredMeasurements, selectedPeriod, currentDate) {
        derivedStateOf {
            MeasurementConverter.measurementsToConsumptionBars(
                filteredMeasurements,
                periodFilter = selectedPeriod,
                referenceDate = currentDate
            )
        }
    }

    val injectionValues by remember(consumptionBars) {
        derivedStateOf { consumptionBars.map { it.injection } }
    }

    val consumptionUnit = stringResource(R.string.consumption_label_kWh)

    // 🔹 Start observing real-time data
    LaunchedEffect(userId) {
        consumptionViewModel.observeAll(userId)
        measurementViewModel.observeRealtime(userId, meterId = "") // Si tu veux filtrer par meterId spécifique
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Title(stringResource(R.string.consumption_label)) }
        item { BannerConsumption() }

        item {
            InstallationFilterById(
                title = "Filter by installation",
                installationIds = installationIds,
                selectedInstallationId = selectedInstallationId,
                onInstallationSelected = {
                    selectedInstallationId = it
                    onFilterSelected(it)
                }
            )
        }

        item {
            PeriodFilterSegmented(
                selected = selectedPeriod,
                onSelectedChange = { selectedPeriod = it }
            )
        }

        item {
            PeriodSwitcher(
                selectedPeriod = selectedPeriod,
                currentDate = currentDate,
                onDateChange = { currentDate = it }
            )
        }

        item {
            ConsumptionInjectionBarChart(
                data = consumptionBars,
                injection = injectionValues,
                maxHeight = 220.dp,
                yValueFormatter = { "${it.roundToInt()} $consumptionUnit" },
                xLabelStep = 1
            )
        }

        item {
            AppTextButton(
                text = "Add Subscription",
                onClick = { showFormDialog = true }
            )
        }

        item {
            ConsumptionColumn(
                consumptions = allConsumptions,
                onPauseToggle = { cons, paused ->
                    // Optional: update pause state in ViewModel
                }
            )
        }
    }

    // 🔹 Form Dialog for adding consumption
    if (showFormDialog) {
        AlertManager.showAlert(
            title = "Add Subscription",
            message = "Fill the form below",
            cancelText = "Cancel",
            confirmText = "Submit",
            onConfirm = { showFormDialog = false },
            onCancel = { showFormDialog = false }
        )

        Dialog(onDismissRequest = { showFormDialog = false }) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                ConsumptionPlanningForm(
                    installationsId = installationIds,
                    lastSubscriptions = allConsumptions
                        .filter { it.installationId != null }
                        .groupBy { it.installationId!! }
                        .mapValues { it.value.maxOf { c -> c.periodEnd } },
                    onSubmit = { installation, start, end, amount ->
                        val newConsumption = Consumption(
                            consumptionId = generateId("C"),
                            installationId = installation,
                            meterId = null,
                            periodStart = start,
                            periodEnd = end,
                            totalEnergy_kWh = amount,
                            totalEnergy_kWhConsummed = 0.0
                        )
                        // 🔹 Save to ViewModel / Firestore
                        consumptionViewModel.addConsumption(userId, newConsumption)
                        globalMessageManager.showMessage(
                            item = "Subscription",
                            type = MessageType.SUCCESS,
                            action = MessageAction.CREATE
                        )
                        showFormDialog = false
                    }
                )
            }
        }
    }

    // 🔹 Snapshot global pour les messages CRUD
    GlobalMessageSnapshot()
}
