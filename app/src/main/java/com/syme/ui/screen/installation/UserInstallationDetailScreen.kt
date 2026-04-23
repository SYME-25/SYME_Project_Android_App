package com.syme.ui.screen.installation

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.syme.R
import com.syme.domain.mapper.allowedApplianceTypes
import com.syme.domain.mapper.labelResId
import com.syme.domain.model.Appliance
import com.syme.domain.model.Circuit
import com.syme.domain.model.Measurement
import com.syme.domain.model.Meter
import com.syme.domain.model.MeterEvent
import com.syme.domain.model.enumeration.ApplianceHeatType
import com.syme.domain.model.enumeration.InstallationType
import com.syme.domain.model.enumeration.Mode
import com.syme.domain.state.UiState
import com.syme.ui.component.card.ApplianceRow
import com.syme.ui.component.card.CircuitRow
import com.syme.ui.component.card.InstallationOverviewCard
import com.syme.ui.component.card.MeterCard
import com.syme.ui.component.card.MeterListItemRow
import com.syme.ui.component.card.PowerBalanceCard
import com.syme.ui.component.compositionlocal.LocalCurrentUserSession
import com.syme.ui.component.dialog.ApplianceDeleteDialog
import com.syme.ui.component.dialog.CircuitDeleteDialog
import com.syme.ui.component.dialog.CircuitEditDialog
import com.syme.ui.component.filter.FilterSection
import com.syme.ui.component.text.SectionHeader
import com.syme.ui.component.text.Title
import com.syme.ui.screen.appliance.components.UserAppliancesList
import com.syme.ui.screen.circuit.CircuitForm
import com.syme.ui.screen.meter.MeterAddForm
import com.syme.ui.viewmodel.ApplianceViewModel
import com.syme.ui.viewmodel.CircuitViewModel
import com.syme.ui.viewmodel.InstallationViewModel
import com.syme.ui.viewmodel.MeterViewModel
import com.syme.utils.applianceCatalog
import com.syme.utils.buildTraceability

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun UserInstallationDetailScreen(
    installationId: String,
    installationViewModel: InstallationViewModel,
    applianceViewModel: ApplianceViewModel,
    meterViewModel: MeterViewModel,
    circuitViewModel: CircuitViewModel,
    onNavigateToApplianceDetail: (Appliance, Mode) -> Unit,
    contentPadding : PaddingValues,
    onBackClick: (() -> Unit)? = null
) {
    val currentUser = LocalCurrentUserSession.current
    val context = LocalContext.current

    val selectedInstallation by installationViewModel.selected.collectAsState()
    val applianceState by applianceViewModel.state.collectAsState()
    val meters by meterViewModel.meters.collectAsState()
    val measurements by meterViewModel.measurements.collectAsState()
    val circuits by circuitViewModel.circuits.collectAsState()

    var selectedInstallationType by remember { mutableStateOf<InstallationType?>(null) }
    var selectedHeatType by remember { mutableStateOf<ApplianceHeatType?>(null) }

    var showAddMeterDialog by remember { mutableStateOf(false) }

    // ⭐ Nouveau state pour meter detail
    var selectedMeter by remember { mutableStateOf<Meter?>(null) }
    var showMeterDialog by remember { mutableStateOf(false) }
    var showAddCircuitDialog by remember { mutableStateOf(false) }

    // Pour le CRUD
    var circuitToEdit   by remember { mutableStateOf<Circuit?>(null) }
    var circuitToDelete by remember { mutableStateOf<Circuit?>(null) }
    var applianceToDelete by remember { mutableStateOf<Appliance?>(null) }

    val latestMeasurement = measurements.lastOrNull() ?: Measurement(meterId = selectedMeter?.meterId ?: "", installationId = installationId)
    val liveSelectedMeter = meters.find { it.meterId == selectedMeter?.meterId }

    // Charger données
    LaunchedEffect(installationId, currentUser?.userId) {
        val userId = currentUser?.userId
        if (!userId.isNullOrBlank()) {
            installationViewModel.getById(userId, installationId)
            applianceViewModel.observe(userId, installationId)
            meterViewModel.observeMeters(userId, installationId)
            circuitViewModel.observeCircuits(userId, installationId)
        }
    }

    // 👇 Nouveau : observer les états relay depuis le Realtime DB pour chaque meter
    LaunchedEffect(meters) {
        val userId = currentUser?.userId ?: return@LaunchedEffect
        meters.forEach { meter ->
            meterViewModel.observeRelaysForMeter(userId, installationId, meter.meterId)
        }
    }

    LaunchedEffect(Unit) {
        meterViewModel.meterEvent.collect { event ->
            when (event) {
                is MeterEvent.Success ->
                    Toast.makeText(context, context.getString(event.messageRes), Toast.LENGTH_SHORT).show()

                is MeterEvent.Error ->
                    Toast.makeText(context, event.arg ?: context.getString(event.messageRes), Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item { Title(
            title = stringResource(R.string.installation_detail),
            padding = 16,
            onBackClick = onBackClick
        ) }

        item { Spacer(Modifier.height(12.dp)) }

        // ── Installation overview card ──────────────────────────────────────────
        item {
            selectedInstallation?.let { installation ->
                InstallationOverviewCard(
                    installation = installation,
                    metersCount = meters.size,
                    circuitsCount = circuits.size
                )
            }
        }

        item { Spacer(Modifier.height(8.dp)) }

        item {
            val userAppliances = (applianceState as? UiState.Success<List<Appliance>>)?.data
            if (!userAppliances.isNullOrEmpty()) {
                PowerBalanceCard(appliances = userAppliances)
            }
        }

        item { HorizontalDivider(Modifier.fillMaxWidth().padding(vertical = 16.dp)) }

        item { Spacer(Modifier.height(16.dp)) }

        item {
            Column {
                FilterSection(
                    title = stringResource(R.string.appliance_filter_type),
                    items = InstallationType.entries,
                    selectedItem = selectedInstallationType,
                    onItemSelected = { selectedInstallationType = it },
                    itemLabel = { stringResource(it.labelResId) },
                    allLabel = stringResource(R.string.home_installation_filter_all)
                )

                Spacer(Modifier.height(6.dp))

                FilterSection(
                    title = stringResource(R.string.appliance_filter_heat_type),
                    items = ApplianceHeatType.entries,
                    selectedItem = selectedHeatType,
                    onItemSelected = { selectedHeatType = it },
                    itemLabel = { stringResource(it.labelResId) }
                )

                Spacer(Modifier.height(6.dp))
            }
        }

        item {
            SectionHeader(
                title = stringResource(R.string.appliance_add_title),
                onAddClick = { },
                isButton = false
            )
        }

        item {
            val filteredCatalog = applianceCatalog
                .let { list ->
                    selectedInstallationType?.let {
                        list.filter { appliance -> appliance.type in it.allowedApplianceTypes }
                    } ?: list
                }
                .let { list ->
                    selectedHeatType?.let {
                        list.filter { appliance -> appliance.heatType == it }
                    } ?: list
                }

            ApplianceRow(
                items = filteredCatalog,
                onClick = { appliance ->
                    onNavigateToApplianceDetail(appliance, Mode.CREATE)
                }
            )
        }

        item { HorizontalDivider(Modifier.fillMaxWidth().padding(vertical = 16.dp)) }

        item {
            SectionHeader(
                title = stringResource(R.string.home_your_appliances),
                onAddClick = { },
                isButton = false
            )
        }

        item {
            when (applianceState) {
                is UiState.Loading -> CircularProgressIndicator()
                is UiState.Success -> {
                    val userAppliances =
                        (applianceState as UiState.Success<List<Appliance>>).data
                    UserAppliancesList(
                        items = userAppliances,
                        onEdit = { appliance ->
                            onNavigateToApplianceDetail(appliance, Mode.EDIT)
                        },
                        onDelete = { applianceToDelete = it }
                    )
                }
                is UiState.Error ->
                    Text(stringResource(R.string.installation_error_loading_installations))
                else -> {}
            }
        }

        item { HorizontalDivider(Modifier.fillMaxWidth().padding(vertical = 16.dp)) }

        item { Spacer(Modifier.height(16.dp)) }

        item {
            SectionHeader(
                title = stringResource(R.string.your_meter),
                onAddClick = { showAddMeterDialog = true }
            )
        }


        item {
            if (meters.isNotEmpty()) {
                MeterListItemRow(
                    items = meters,
                    onClick = { meter ->
                        selectedMeter = meter
                        showMeterDialog = true

                        val userId = currentUser?.userId ?: return@MeterListItemRow
                        meterViewModel.startRealtimeAggregation(
                            userId,
                            installationId,
                            meter.meterId
                        )
                    }
                )
            } else {
                Text(stringResource(R.string.no_meter_found))
            }
        }

        item { HorizontalDivider(Modifier.fillMaxWidth().padding(vertical = 16.dp)) }

        item {
            SectionHeader(
                title = stringResource(R.string.your_circuits),
                onAddClick = { showAddCircuitDialog = true }
            )
        }

        item {
            if (circuits.isNotEmpty()) {
                CircuitRow(
                    items = circuits,
                    onClick = { /* futur détail */ },
                    onEdit = { circuitToEdit   = it },
                    onDelete = { circuitToDelete = it }
                )
            } else {
                Text(stringResource(R.string.no_circuit_found))
            }
        }

        item {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(contentPadding.calculateBottomPadding() + 100.dp)
            )
        }

    }

    // -------------------------
    // Dialog Ajout Meter
    // -------------------------
    if (showAddMeterDialog) {
        Dialog(onDismissRequest = { showAddMeterDialog = false }) {
            Surface(
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.padding(16.dp)
            ) {
                MeterAddForm { meterId, securityCode ->
                    val userId = currentUser?.userId ?: return@MeterAddForm
                    meterViewModel.loadMeter(userId, installationId, meterId, securityCode)

                    val updatedInstallation = selectedInstallation?.copy(trace = buildTraceability(selectedInstallation?.trace, userId, isActive = true))
                    installationViewModel.update(userId, updatedInstallation!!)

                    showAddMeterDialog = false
                }
            }
        }
    }

    // -------------------------
    // Dialog Meter Detail (animé)
    // -------------------------
    if (showMeterDialog && liveSelectedMeter != null) {
        Dialog(
            onDismissRequest = {
                showMeterDialog = false
                selectedMeter = null
                meterViewModel.stopRealtime()
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {

                AnimatedVisibility(
                    visible = showMeterDialog,
                    enter = fadeIn(tween(250)) + scaleIn(initialScale = 0.92f, animationSpec = tween(250)),
                    exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.92f, animationSpec = tween(200))
                ) {

                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        modifier = Modifier
                            .fillMaxWidth(1f)
                            .padding(8.dp)
                    ) {
                        if (latestMeasurement != null) {
                            MeterCard(
                                meterId = liveSelectedMeter.meterId,
                                meterState = liveSelectedMeter.status.name,
                                measurement = latestMeasurement,
                                relays = liveSelectedMeter.relays,
                                onRelayToggle = { relay, _ ->
                                    val userId = currentUser?.userId ?: return@MeterCard
                                    meterViewModel.toggleRelay(
                                        userId,
                                        installationId,
                                        relay
                                    )
                                }
                            )
                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddCircuitDialog) {
        Dialog(
            onDismissRequest = { showAddCircuitDialog = false }
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.padding(16.dp)
            ) {
                CircuitForm(
                    meters = meters,
                    onSave = { meterId, relayChannel, name, priority, isProtected ->

                        val userId = currentUser?.userId ?: return@CircuitForm

                        val nextCircuitId = (circuits.maxOfOrNull {
                            it.circuitId
                        } ?: 0) + 1

                        val circuit = Circuit(
                            circuitId = nextCircuitId, // 👈 auto incrément
                            installationId = installationId,
                            meterId = meterId,
                            relayChannel = relayChannel,
                            name = name,
                            priority = priority,
                            isProtected = isProtected,
                            trace = buildTraceability(null, userId, isActive = true)
                        )

                        circuitViewModel.addCircuit(
                            userId,
                            installationId,
                            circuit
                        )

                        showAddCircuitDialog = false
                    }
                )
            }
        }
    }

    // ── Circuit edit ──
    circuitToEdit?.let { circuit ->
        CircuitEditDialog (
            circuit  = circuit,
            meters   = meters,
            onDismiss = { circuitToEdit = null },
            onConfirm = { updated ->
                val userId = currentUser?.userId ?: return@CircuitEditDialog
                circuitViewModel.updateCircuit(userId, installationId, updated)
                circuitToEdit = null
            }
        )
    }

    // ── Circuit delete ──
    circuitToDelete?.let { circuit ->
        CircuitDeleteDialog (
            circuit   = circuit,
            onDismiss = { circuitToDelete = null },
            onConfirm = {
                val userId = currentUser?.userId ?: return@CircuitDeleteDialog
                circuitViewModel.deleteCircuit(userId, installationId, circuit.circuitId.toString())
                circuitToDelete = null
            }
        )
    }

    // ── Appliance delete ──
    applianceToDelete?.let { appliance ->
        ApplianceDeleteDialog(
            appliance = appliance,
            onDismiss = { applianceToDelete = null },
            onConfirm = {
                val userId = currentUser?.userId ?: return@ApplianceDeleteDialog
                applianceViewModel.delete(userId, installationId, appliance)
                applianceToDelete = null
            }
        )
    }


}