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
import androidx.compose.ui.window.DialogProperties
import com.syme.R
import com.syme.domain.mapper.ConsumptionBarFactory
import com.syme.domain.model.Consumption
import com.syme.utils.MeasurementUtil
import com.syme.domain.model.Installation
import com.syme.domain.model.enumeration.ConsumptionFormType
import com.syme.domain.model.enumeration.ConsumptionStateType
import com.syme.domain.model.enumeration.PeriodFilter
import com.syme.ui.alerts.AlertManager
import com.syme.ui.component.card.ConsumptionRow
import com.syme.ui.component.chart.ConsumptionInjectionBarChart
import com.syme.ui.component.compositionlocal.LocalCurrentUserSession
import com.syme.ui.component.text.SectionHeader
import com.syme.ui.component.text.Title
import com.syme.ui.snapshot.MessageAction
import com.syme.ui.snapshot.MessageType
import com.syme.ui.snapshot.globalMessageManager
import com.syme.ui.state.UiState
import com.syme.ui.viewmodel.ConsumptionViewModel
import com.syme.ui.viewmodel.InstallationViewModel
import com.syme.ui.viewmodel.MeterViewModel
import com.syme.utils.generateId
import java.time.LocalDate
import kotlin.math.round
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumptionScreen(
    installationViewModel: InstallationViewModel,
    consumptionViewModel: ConsumptionViewModel,
    meterViewModel: MeterViewModel,
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

    val meters by meterViewModel.meters.collectAsState()

    var selectedPeriod by remember { mutableStateOf(PeriodFilter.DAY) }
    var currentDate by remember { mutableStateOf(LocalDate.now()) }
    var showFormDialog by remember { mutableStateOf(false) }

    // 🔹 Observed states
    val consumptions by consumptionViewModel.consumptions.collectAsState()
    val measurements by meterViewModel.aggregatedMeasurements.collectAsState()

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
            MeasurementUtil.filterMeasurementsByPeriod(byInstallation, selectedPeriod, currentDate)
        }
    }

    // 🔹 Barres de consommation pour le chart
    val consumptionBars by remember(
        filteredMeasurements,
        selectedPeriod,
        currentDate,
        consumptions
    ) {
        derivedStateOf {
            ConsumptionBarFactory.build(
                measurements = filteredMeasurements,
                consumptions = consumptions,
                periodFilter = selectedPeriod,
                referenceDate = currentDate
            )
        }
    }

    val injectionValues by remember(consumptionBars) { derivedStateOf { consumptionBars.map { it.injection } } }
    val consumptionUnit = stringResource(R.string.consumption_label_Wh)

    val hasSubscriptionByInstallation = remember(allConsumptions) {
        allConsumptions
            .filter { !it.onDemand && it.installationId != null }
            .groupBy { cons ->
                installationsMap.entries
                    .firstOrNull { it.value == cons.installationId }
                    ?.key
            }
            .filterKeys { it != null }
            .mapKeys { it.key!! }
            .mapValues { true }
    }

    // 🔹 Observer installations
    LaunchedEffect(userId) {
        if (userId.isNotBlank()) installationViewModel.observe(userId)
    }

    // 🔹 Observer consommations pour l’installation sélectionnée
    LaunchedEffect(userId, selectedInstallationId) {
        if (userId.isNotBlank() && !selectedInstallationId.isNullOrBlank()) {
            consumptionViewModel.observeAll(userId, selectedInstallationId!!)
            meterViewModel.observeMeters(userId, selectedInstallationId!!)
            meterViewModel.observeAggregatedMeasurements(userId, selectedInstallationId!!)
        }
    }

    LaunchedEffect(installationsMap) {
        if (selectedInstallationId == null && installationsMap.isNotEmpty()) {
            selectedInstallationId = installationsMap.values.first()
        }
    }

    LaunchedEffect(meters, selectedInstallationId) {
        val installationId = selectedInstallationId ?: return@LaunchedEffect
        if (userId.isBlank()) return@LaunchedEffect
        val activeMeter = meters.firstOrNull { it.status.name == "ACTIVE" }
            ?: meters.firstOrNull()
        if (activeMeter != null) {
            meterViewModel.startBackgroundAggregationIfNeeded(
                userId, installationId, activeMeter.meterId
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            meterViewModel.stopRealtime()
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

        item { Spacer(modifier = Modifier.height(8.dp)) }
        item {
            HorizontalDivider(modifier = Modifier.fillMaxWidth().height(1.dp))
        }
        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            SectionHeader(
                title = stringResource(R.string.your_consumption_planning),
                onAddClick = { showFormDialog = true }
            )
        }

        // 🔹 Liste consommation
        item {
            ConsumptionRow(
                consumptions = allConsumptions,
                realtimeMeasurements = measurements,
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

    // 🔹 Form Dialog (Subscription / Demand avec Tabs)
    if (showFormDialog) {

        var selectedForm by remember { mutableStateOf(ConsumptionFormType.SUBSCRIPTION) }

        Dialog(
            onDismissRequest = { showFormDialog = false },
            properties = DialogProperties(
                    usePlatformDefaultWidth = false
                )
            ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column {

                    // ── Tabs ─────────────────────────────────────────────
                    TabRow(selectedTabIndex = selectedForm.ordinal) {
                        Tab(
                            selected = selectedForm == ConsumptionFormType.SUBSCRIPTION,
                            onClick = { selectedForm = ConsumptionFormType.SUBSCRIPTION },
                            text = { Text(stringResource(R.string.subscription_title)) }
                        )
                        Tab(
                            selected = selectedForm == ConsumptionFormType.DEMAND,
                            onClick = { selectedForm = ConsumptionFormType.DEMAND },
                            text = { Text(stringResource(R.string.demand_title)) }
                        )
                    }

                    // ── Data préparées ───────────────────────────────────
                    val lastSubscriptions = remember(allConsumptions) {
                        allConsumptions
                            .filter { !it.onDemand && it.installationId != null }
                            .groupBy { cons ->
                                installationsMap.entries
                                    .firstOrNull { it.value == cons.installationId }
                                    ?.key
                            }
                            .filterKeys { it != null }
                            .mapKeys { it.key!! }
                            .mapValues { it.value.maxOf { c -> c.periodEnd } }
                    }

                    val lastDemands = remember(allConsumptions) {
                        allConsumptions
                            .filter { it.onDemand && it.installationId != null }
                            .groupBy { cons ->
                                installationsMap.entries
                                    .firstOrNull { it.value == cons.installationId }
                                    ?.key
                            }
                            .filterKeys { it != null }
                            .mapKeys { it.key!! }
                            .mapValues { it.value.maxOf { c -> c.periodEnd } }
                    }

                    // ⚡ puissance souscrite par installation (exemple basé sur ton modèle Installation)
                    val powerSubscribedByInstallation = remember(installationState) {
                        when (installationState) {
                            is UiState.Success -> {
                                (installationState as UiState.Success<List<Installation>>)
                                    .data.associate { it.name to it.powerSubscribed }
                            }
                            else -> emptyMap()
                        }
                    }

                    // ── Formulaire selon l’onglet ────────────────────────
                    when (selectedForm) {

                        ConsumptionFormType.SUBSCRIPTION -> {
                            SubscriptionForm(
                                installationsId = installationNames,
                                lastSubscriptions = lastSubscriptions,
                                onSubmit = { installationName, start, end, energyWh ->

                                    val installationId =
                                        installationsMap[installationName] ?: return@SubscriptionForm

                                    val newConsumption = Consumption(
                                        consumptionId = generateId("C"),
                                        installationId = installationId,
                                        meterId = null,
                                        periodStart = start,
                                        periodEnd = end,
                                        totalEnergy_kWh = energyWh.roundToInt(),
                                        totalEnergy_kWhConsummed = 0.0,
                                        onDemand = false
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

                        ConsumptionFormType.DEMAND -> {

                            val suggestedPowersKw = remember(selectedInstallationId, powerSubscribedByInstallation) {

                                val installationName = installationsMap.entries
                                    .firstOrNull { it.value == selectedInstallationId }
                                    ?.key

                                val subscribedPower = powerSubscribedByInstallation[installationName] ?: 0.0

                                if (subscribedPower == 0.0) {
                                    emptyList()
                                } else {
                                    listOf(
                                        round(subscribedPower * 0.75),
                                        round(subscribedPower * 0.5),
                                        round(subscribedPower * 0.25)
                                    )
                                }
                            }

                            DemandForm(
                                installationsId = installationNames,
                                lastDemandEnds = lastDemands,
                                powerSubscribedByInstallation = powerSubscribedByInstallation,
                                hasSubscriptionByInstallation = hasSubscriptionByInstallation,
                                suggestedPowersKw = suggestedPowersKw,
                                onSubmit = { installationName, start, end, requestedPowerKw ->

                                    val installationId =
                                        installationsMap[installationName] ?: return@DemandForm

                                    val newConsumption = Consumption(
                                        consumptionId = generateId("D"),
                                        installationId = installationId,
                                        meterId = null,
                                        periodStart = start,
                                        periodEnd = end,
                                        totalEnergy_kWh = 0,
                                        totalEnergy_kWhConsummed = 0.0,
                                        onDemand = true,
                                        requestedPowerKw = requestedPowerKw
                                    )

                                    consumptionViewModel.addConsumption(
                                        userId = userId,
                                        installationId = installationId,
                                        consumption = newConsumption
                                    )

                                    globalMessageManager.showMessage(
                                        item = "Demand",
                                        type = MessageType.SUCCESS,
                                        action = MessageAction.CREATE
                                    )
                                    showFormDialog = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
