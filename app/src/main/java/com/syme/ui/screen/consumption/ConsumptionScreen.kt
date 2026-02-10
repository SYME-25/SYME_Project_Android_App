package com.syme.ui.screen.consumption

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.syme.R
import com.syme.domain.mapper.MeasurementConverter
import com.syme.domain.model.Consumption
import com.syme.domain.model.Installation
import com.syme.domain.model.enumeration.ConsumptionStateType
import com.syme.domain.model.enumeration.PeriodFilter
import com.syme.ui.alerts.AlertManager
import com.syme.ui.component.actionbutton.AppTextButton
import com.syme.ui.component.card.ConsumptionRow
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

    // 🔹 Sélection par défaut (première installation si disponible)
    var selectedInstallationId by remember {
        mutableStateOf(installationsMap.values.firstOrNull())
    }

    var selectedPeriod by remember { mutableStateOf(PeriodFilter.DAY) }
    var currentDate by remember { mutableStateOf(LocalDate.now()) }
    var showFormDialog by remember { mutableStateOf(false) }

    // 🔹 Observed states
    val consumptions by consumptionViewModel.consumptions.collectAsState()
    val measurements by measurementViewModel.measurements.collectAsState()

    // 🔹 Consommations filtrées pour l’installation sélectionnée
    val allConsumptions = remember(consumptions, selectedInstallationId) {
        consumptions
            .filter { it.installationId == selectedInstallationId }
            .sortedByDescending { it.periodStart }
            .take(50)
            .toMutableStateList()
    }

    // 🔹 Toutes les mesures pour chart (pas de filtre meter pour l'instant)
    val filteredMeasurements by remember(measurements, selectedInstallationId, selectedPeriod, currentDate) {
        derivedStateOf {
            val byInstallation = if (selectedInstallationId != null) {
                measurements.filter { it.installationId == selectedInstallationId }
            } else measurements
            MeasurementConverter.filterMeasurementsByPeriod(byInstallation, selectedPeriod, currentDate)
        }
    }

    // 🔹 Barres de consommation pour le chart
    val consumptionBars by remember(filteredMeasurements, selectedPeriod, currentDate) {
        derivedStateOf {
            MeasurementConverter.measurementsToConsumptionBars(
                filteredMeasurements,
                periodFilter = selectedPeriod,
                referenceDate = currentDate
            )
        }
    }
    val injectionValues by remember(consumptionBars) { derivedStateOf { consumptionBars.map { it.injection } } }
    val consumptionUnit = stringResource(R.string.consumption_label_kWh)

    // 🔹 Observer installations
    LaunchedEffect(userId) {
        if (userId.isNotBlank()) installationViewModel.observe(userId)
    }

    // 🔹 Observer mesures en temps réel pour l’installation
    LaunchedEffect(userId, selectedInstallationId) {
        if (!userId.isNullOrBlank() && !selectedInstallationId.isNullOrBlank()) {
            measurementViewModel.observeRealtime(
                userId = userId,
                installationId = selectedInstallationId!!,
                meterId = "1"
            )
        }
    }

    // 🔹 Observer consommations pour l’installation sélectionnée
    LaunchedEffect(userId, selectedInstallationId) {
        if (!userId.isNullOrBlank() && !selectedInstallationId.isNullOrBlank()) {
            consumptionViewModel.observeAll(userId, selectedInstallationId!!)
        }
    }

    // 🔹 UI
    LazyColumn(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Title(stringResource(R.string.consumption_label)) }
        item { BannerConsumption() }

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

        // 🔹 Filtre période
        item {
            PeriodFilterSegmented(
                selected = selectedPeriod,
                onSelectedChange = { selectedPeriod = it }
            )
        }

        // 🔹 Switcher date
        item {
            PeriodSwitcher(
                selectedPeriod = selectedPeriod,
                currentDate = currentDate,
                onDateChange = { currentDate = it }
            )
        }

        // 🔹 Chart
        item {
            ConsumptionInjectionBarChart(
                data = consumptionBars,
                injection = injectionValues,
                maxHeight = 220.dp,
                yValueFormatter = { "${it.roundToInt()} $consumptionUnit" },
                xLabelStep = 1,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
        item {
            HorizontalDivider(modifier = Modifier.fillMaxWidth().height(1.dp))
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }

        // 🔹 Bouton ajout
        item {
            AppTextButton(
                text = stringResource(R.string.consumption_button_add),
                onClick = { showFormDialog = true }
            )
        }

        // 🔹 Liste consommation
        item {
            ConsumptionRow(
                consumptions = allConsumptions,
                onPauseToggle = { cons, paused ->
                    val instId = cons.installationId ?: return@ConsumptionRow
                    val updated = cons.copy(
                        consumptionState = if (paused)
                            ConsumptionStateType.PAUSED
                        else
                            ConsumptionStateType.RUNNING
                    )
                    consumptionViewModel.updateConsumption(userId, instId, updated)
                }
            )
        }
    }

    // 🔹 Form Dialog
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
                    installationsId = installationNames,
                    lastSubscriptions = allConsumptions
                        .filter { it.installationId != null }
                        .groupBy { cons -> installationsMap.entries.firstOrNull { it.value == cons.installationId }?.key }
                        .filterKeys { it != null }
                        .mapKeys { it.key!! }
                        .mapValues { it.value.maxOf { c -> c.periodEnd } },
                    onSubmit = { installationName, start, end, energyWh ->
                        val installationId = installationsMap[installationName] ?: return@ConsumptionPlanningForm

                        val newConsumption = Consumption(
                            consumptionId = generateId("C"),
                            installationId = installationId,
                            meterId = null,
                            periodStart = start,
                            periodEnd = end,
                            totalEnergy_kWh = energyWh.roundToInt(),
                            totalEnergy_kWhConsummed = 0.0
                        )

                        consumptionViewModel.addConsumption(
                            userId = userId,
                            installationId = installationId,
                            consumption = newConsumption
                        )

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

    // 🔹 Snapshot global pour messages CRUD
    GlobalMessageSnapshot()
}
