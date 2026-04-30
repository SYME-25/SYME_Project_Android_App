package com.syme.ui.screen.consumption

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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
import com.syme.domain.model.Installation
import com.syme.domain.model.InstallationConsumptionEntry
import com.syme.domain.model.enumeration.ConsumptionFormType
import com.syme.domain.model.enumeration.ConsumptionStateType
import com.syme.domain.model.enumeration.PeriodFilter
import com.syme.domain.state.UiState
import com.syme.ui.component.animation.banner.BannerConsumption
import com.syme.ui.component.card.ConsumptionRow
import com.syme.ui.component.chart.ConsumptionInjectionBarChart
import com.syme.ui.component.chart.InstallationComparisonChart
import com.syme.ui.component.compositionlocal.LocalCurrentUserSession
import com.syme.ui.component.filter.FilterSection
import com.syme.ui.component.filter.SegmentedControl
import com.syme.ui.component.state.EmptyStatePlaceholder
import com.syme.ui.component.text.SectionHeader
import com.syme.ui.component.text.Title
import com.syme.ui.screen.consumption.components.PeriodFilterSegmented
import com.syme.ui.screen.consumption.components.PeriodSwitcher
import com.syme.ui.snapshot.MessageAction
import com.syme.ui.snapshot.MessageType
import com.syme.ui.snapshot.globalMessageManager
import com.syme.ui.viewmodel.ConsumptionViewModel
import com.syme.ui.viewmodel.InstallationViewModel
import com.syme.ui.viewmodel.MeterViewModel
import com.syme.utils.MeasurementUtil
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
    onFilterSelected: (String?) -> Unit = {},
    contentPadding: PaddingValues
) {
    val currentUser = LocalCurrentUserSession.current
    val userId = currentUser?.userId ?: ""

    val installationState by installationViewModel.state.collectAsState()

    val installationsMap = remember(installationState) {
        when (installationState) {
            is UiState.Success ->
                (installationState as UiState.Success<List<Installation>>)
                    .data.associate { it.name to it.installationId }
            else -> emptyMap()
        }
    }
    val installationNames = installationsMap.keys.toList()

    var selectedInstallationId by remember { mutableStateOf(installationsMap.values.firstOrNull()) }

    val meters by meterViewModel.meters.collectAsState()
    var selectedPeriod by remember { mutableStateOf(PeriodFilter.DAY) }
    var currentDate by remember { mutableStateOf(LocalDate.now()) }
    var showFormDialog by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableStateOf(0) }

    val consumptions by consumptionViewModel.consumptions.collectAsState()
    val measurements by meterViewModel.aggregatedMeasurements.collectAsState()

    val measurementsByInstallation = remember { mutableStateMapOf<String, List<com.syme.domain.model.Measurement>>() }

    LaunchedEffect(measurements, selectedInstallationId) {
        val id = selectedInstallationId ?: return@LaunchedEffect
        if (measurements.isNotEmpty()) {
            measurementsByInstallation[id] = measurements
        }
    }

    val filteredMeasurements by remember(measurements, selectedInstallationId, selectedPeriod, currentDate) {
        derivedStateOf {
            val byInstallation = if (selectedInstallationId != null) {
                measurements.filter { it.installationId == selectedInstallationId }
            } else measurements
            MeasurementUtil.filterMeasurementsByPeriod(byInstallation, selectedPeriod, currentDate)
        }
    }

    // ✅ FIX : utilisé directement sans cache lastValid* qui bloquait les mises à jour
    val consumptionBars by remember(filteredMeasurements, selectedPeriod, currentDate, consumptions) {
        derivedStateOf {
            ConsumptionBarFactory.build(
                measurements = filteredMeasurements,
                consumptions = consumptions,
                periodFilter = selectedPeriod,
                referenceDate = currentDate
            )
        }
    }

    val installationConsumptionEntries by remember(
        measurementsByInstallation.toMap(), consumptions, installationsMap, selectedInstallationId
    ) {
        derivedStateOf {
            installationsMap.entries.mapNotNull { (name, id) ->
                val cachedMeasurements = measurementsByInstallation[id]
                val totalFromMeasurements = if (!cachedMeasurements.isNullOrEmpty()) {
                    cachedMeasurements.mapNotNull { it.energyActiveWh }.sum()
                } else null
                val totalFromConsumptions = consumptions
                    .filter { it.installationId == id }
                    .sumOf { it.totalEnergy_kWhConsummed * 1000.0 }
                val totalWh = totalFromMeasurements
                    ?: if (totalFromConsumptions > 0) totalFromConsumptions else null
                totalWh?.let {
                    InstallationConsumptionEntry(
                        installationName = name,
                        totalEnergyWh = it,
                        isSelected = id == selectedInstallationId
                    )
                }
            }
        }
    }

    val injectionValues by remember(consumptionBars) {
        derivedStateOf { consumptionBars.map { it.injection } }
    }

    val consumptionUnit = stringResource(R.string.consumption_label_Wh)

    val planningConsumptions by remember(consumptions, selectedInstallationId) {
        derivedStateOf {
            consumptions
                .filter { it.installationId == selectedInstallationId }
                .sortedByDescending { it.periodStart }
                .take(50)
        }
    }

    val hasSubscriptionByInstallation = remember(planningConsumptions) {
        planningConsumptions
            .filter { !it.onDemand && it.installationId != null }
            .groupBy { cons ->
                installationsMap.entries.firstOrNull { it.value == cons.installationId }?.key
            }
            .filterKeys { it != null }
            .mapKeys { it.key!! }
            .mapValues { true }
    }

    // ─── LaunchedEffects ──────────────────────────────────────────────────────

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) installationViewModel.observe(userId)
    }

    // ✅ FIX : installationsMap retiré de la clé — il est recréé à chaque recomposition
    // ce qui annulait/relançait observeAggregatedMeasurements en boucle, coupant le flux Firestore
    LaunchedEffect(userId, selectedInstallationId) {
        if (userId.isNotBlank() && !selectedInstallationId.isNullOrBlank()) {
            consumptionViewModel.observeAll(userId, selectedInstallationId!!)
            meterViewModel.observeMeters(userId, selectedInstallationId!!)
            meterViewModel.observeAggregatedMeasurements(userId, selectedInstallationId!!)
        }
    }

    LaunchedEffect(userId, installationsMap) {
        if (userId.isBlank()) return@LaunchedEffect
        installationsMap.values
            .filter { it != selectedInstallationId }
            .forEach { installationId ->
                meterViewModel.observeAggregatedMeasurementsForComparison(userId, installationId) { list ->
                    if (list.isNotEmpty()) measurementsByInstallation[installationId] = list
                }
            }
    }

    LaunchedEffect(installationsMap) {
        if (installationsMap.isNotEmpty() && selectedInstallationId == null) {
            selectedInstallationId = installationsMap.values.first()
        }
    }

    LaunchedEffect(meters, selectedInstallationId) {
        val installationId = selectedInstallationId ?: return@LaunchedEffect
        if (userId.isBlank()) return@LaunchedEffect
        val activeMeter = meters.firstOrNull { it.status.name == "ACTIVE" } ?: meters.firstOrNull()
        if (activeMeter != null) {
            meterViewModel.startBackgroundAggregationIfNeeded(userId, installationId, activeMeter.meterId)
        }
    }

    LaunchedEffect(consumptions, installationState) {
        val now = System.currentTimeMillis()
        val installations = (installationState as? UiState.Success<List<Installation>>)?.data
            ?: return@LaunchedEffect
        installations.forEach { installation ->
            val instId = installation.installationId
            val currentDemand = consumptions.firstOrNull { c ->
                c.installationId == instId && c.onDemand &&
                        c.periodStart <= now && now <= c.periodEnd &&
                        c.consumptionState != ConsumptionStateType.PAUSED
            }
            val currentSubscription = consumptions.firstOrNull { c ->
                c.installationId == instId && !c.onDemand &&
                        c.periodStart <= now && now <= c.periodEnd &&
                        c.consumptionState != ConsumptionStateType.PAUSED
            }
            val lastSubscription = consumptions
                .filter { c -> c.installationId == instId && !c.onDemand }
                .maxByOrNull { it.periodEnd }
            val resolvedPower = when {
                currentDemand != null && currentDemand.requestedPowerKw > 0 -> currentDemand.requestedPowerKw
                currentSubscription != null && currentSubscription.requestedPowerKw > 0 -> currentSubscription.requestedPowerKw
                lastSubscription != null && lastSubscription.requestedPowerKw > 0 -> lastSubscription.requestedPowerKw
                else -> null
            }
            if (resolvedPower != null && resolvedPower != installation.powerSubscribed) {
                installationViewModel.update(userId, installation.copy(powerSubscribed = resolvedPower))
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { meterViewModel.stopRealtime() }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI
    // ─────────────────────────────────────────────────────────────────────────

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Title(stringResource(R.string.consumption_label), padding = 0)
        Spacer(modifier = Modifier.height(12.dp))
        BannerConsumption()
        Spacer(modifier = Modifier.height(20.dp))

        SegmentedControl(
            tabs = listOf(
                stringResource(R.string.tab_analysis),
                stringResource(R.string.tab_planning)
            ),
            selectedIndex = selectedTabIndex,
            onTabSelected = { selectedTabIndex = it }
        )

        Spacer(modifier = Modifier.height(20.dp))

        when (selectedTabIndex) {
            0 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        if (installationNames.isNotEmpty()) {
                            FilterSection(
                                title = stringResource(R.string.consumption_filter_by_installation),
                                items = installationNames,
                                selectedItem = installationsMap.entries.firstOrNull { it.value == selectedInstallationId }?.key,
                                onItemSelected = { selectedName ->
                                    selectedInstallationId = installationsMap[selectedName]
                                    onFilterSelected(selectedInstallationId)
                                },
                                itemLabel = { name -> if (name.length > 12) name.take(12) + "…" else name },
                                showAll = false
                            )
                        }
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

                    // ✅ FIX : consumptionBars utilisé directement — le cache lastValid* empêchait
                    // les mises à jour quand consumptionBars devenait temporairement vide
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

                    item {
                        if (installationConsumptionEntries.size >= 2) {
                            InstallationComparisonChart(
                                entries = installationConsumptionEntries,
                                modifier = Modifier.padding(horizontal = 0.dp)
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(contentPadding.calculateBottomPadding() + 32.dp)) }
                }
            }

            1 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        if (installationNames.isNotEmpty()) {
                            FilterSection(
                                title = stringResource(R.string.consumption_filter_by_installation),
                                items = installationNames,
                                selectedItem = installationsMap.entries.firstOrNull { it.value == selectedInstallationId }?.key,
                                onItemSelected = { selectedName ->
                                    selectedInstallationId = installationsMap[selectedName]
                                },
                                itemLabel = { name -> if (name.length > 12) name.take(12) + "…" else name },
                                showAll = false
                            )
                        }
                    }

                    item {
                        SectionHeader(
                            title = stringResource(R.string.your_consumption_planning),
                            onAddClick = { showFormDialog = true }
                        )
                    }

                    item {
                        if (planningConsumptions.isEmpty()) {
                            EmptyStatePlaceholder(
                                icon = Icons.Default.DateRange,
                                title = stringResource(R.string.no_planning_title),
                                description = stringResource(R.string.no_planning_description)
                            )
                        } else {
                            ConsumptionRow(
                                consumptions = planningConsumptions,
                                realtimeMeasurements = measurements,
                                onPauseToggle = { cons, paused ->
                                    val instId = cons.installationId ?: return@ConsumptionRow
                                    val updated = cons.copy(
                                        consumptionState = if (paused) ConsumptionStateType.PAUSED else ConsumptionStateType.RUNNING
                                    )
                                    consumptionViewModel.updateConsumption(userId, instId, updated)
                                }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(contentPadding.calculateBottomPadding() + 32.dp)) }
                }
            }
        }
    }

    if (showFormDialog) {
        var selectedForm by remember { mutableStateOf(ConsumptionFormType.SUBSCRIPTION) }

        Dialog(
            onDismissRequest = { showFormDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column {
                    SegmentedControl(
                        tabs = listOf(
                            stringResource(R.string.subscription_title),
                            stringResource(R.string.demand_title)
                        ),
                        selectedIndex = selectedForm.ordinal,
                        onTabSelected = { selectedForm = ConsumptionFormType.entries[it] }
                    )

                    val lastSubscriptions = remember(planningConsumptions) {
                        planningConsumptions
                            .filter { !it.onDemand && it.installationId != null }
                            .groupBy { cons ->
                                installationsMap.entries.firstOrNull { it.value == cons.installationId }?.key
                            }
                            .filterKeys { it != null }
                            .mapKeys { it.key!! }
                            .mapValues { it.value.maxOf { c -> c.periodEnd } }
                    }

                    val lastDemands = remember(planningConsumptions) {
                        planningConsumptions
                            .filter { it.onDemand && it.installationId != null }
                            .groupBy { cons ->
                                installationsMap.entries.firstOrNull { it.value == cons.installationId }?.key
                            }
                            .filterKeys { it != null }
                            .mapKeys { it.key!! }
                            .mapValues { it.value.maxOf { c -> c.periodEnd } }
                    }

                    val powerSubscribedByInstallation = remember(installationState) {
                        when (installationState) {
                            is UiState.Success ->
                                (installationState as UiState.Success<List<Installation>>)
                                    .data.associate { it.name to it.powerSubscribed }
                            else -> emptyMap()
                        }
                    }

                    when (selectedForm) {
                        ConsumptionFormType.SUBSCRIPTION -> {
                            SubscriptionForm(
                                installationsId = installationNames,
                                lastSubscriptions = lastSubscriptions,
                                onSubmit = { installationName, start, end, energyWh, powerKw ->
                                    val installationId = installationsMap[installationName] ?: return@SubscriptionForm
                                    val now = System.currentTimeMillis()
                                    consumptionViewModel.addConsumption(
                                        userId, installationId,
                                        Consumption(
                                            consumptionId = generateId("C"),
                                            installationId = installationId,
                                            meterId = null,
                                            periodStart = start, periodEnd = end,
                                            totalEnergy_kWh = energyWh.roundToInt(),
                                            totalEnergy_kWhConsummed = 0.0,
                                            onDemand = false, requestedPowerKw = powerKw
                                        )
                                    )
                                    if (now in start..end) {
                                        val hasActiveDemand = consumptions.any { c ->
                                            c.installationId == installationId && c.onDemand &&
                                                    c.periodStart <= now && now <= c.periodEnd &&
                                                    c.consumptionState != ConsumptionStateType.PAUSED
                                        }
                                        if (!hasActiveDemand) {
                                            val installation = (installationState as? UiState.Success<List<Installation>>)
                                                ?.data?.find { it.installationId == installationId }
                                            if (installation != null && powerKw > 0.0) {
                                                installationViewModel.update(userId, installation.copy(powerSubscribed = powerKw))
                                            }
                                        }
                                    }
                                    globalMessageManager.showMessage(item = "Subscription", type = MessageType.SUCCESS, action = MessageAction.CREATE)
                                    showFormDialog = false
                                }
                            )
                        }

                        ConsumptionFormType.DEMAND -> {
                            val suggestedPowersKw = remember(selectedInstallationId, powerSubscribedByInstallation) {
                                val installationName = installationsMap.entries
                                    .firstOrNull { it.value == selectedInstallationId }?.key
                                val subscribedPower = powerSubscribedByInstallation[installationName] ?: 0.0
                                if (subscribedPower == 0.0) emptyList()
                                else listOf(
                                    round(subscribedPower * 0.75),
                                    round(subscribedPower * 0.5),
                                    round(subscribedPower * 0.25)
                                )
                            }
                            DemandForm(
                                installationsId = installationNames,
                                lastDemandEnds = lastDemands,
                                powerSubscribedByInstallation = powerSubscribedByInstallation,
                                hasSubscriptionByInstallation = hasSubscriptionByInstallation,
                                suggestedPowersKw = suggestedPowersKw,
                                onSubmit = { installationName, start, end, requestedPowerKw ->
                                    val installationId = installationsMap[installationName] ?: return@DemandForm
                                    val now = System.currentTimeMillis()
                                    consumptionViewModel.addConsumption(
                                        userId, installationId,
                                        Consumption(
                                            consumptionId = generateId("D"),
                                            installationId = installationId,
                                            meterId = null,
                                            periodStart = start, periodEnd = end,
                                            totalEnergy_kWh = 0, totalEnergy_kWhConsummed = 0.0,
                                            onDemand = true, requestedPowerKw = requestedPowerKw
                                        )
                                    )
                                    if (now in start..end) {
                                        val installation = (installationState as? UiState.Success<List<Installation>>)
                                            ?.data?.find { it.installationId == installationId }
                                        if (installation != null && requestedPowerKw > 0.0) {
                                            installationViewModel.update(userId, installation.copy(powerSubscribed = requestedPowerKw))
                                        }
                                    }
                                    globalMessageManager.showMessage(item = "Demand", type = MessageType.SUCCESS, action = MessageAction.CREATE)
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