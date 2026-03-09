package com.syme.ui.screen.installation

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.syme.domain.mapper.imageResId
import com.syme.domain.model.*
import com.syme.domain.model.enumeration.ApplianceHeatType
import com.syme.domain.model.enumeration.InstallationType
import com.syme.ui.component.card.*
import com.syme.ui.component.compositionlocal.LocalCurrentUserSession
import com.syme.ui.component.text.SectionHeader
import com.syme.ui.component.text.Title
import com.syme.ui.screen.appliance.*
import com.syme.ui.screen.circuit.CircuitForm
import com.syme.ui.screen.meter.MeterAddForm
import com.syme.ui.state.UiState
import com.syme.ui.viewmodel.*
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
    onApplianceClick: (Appliance) -> Unit = {},
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
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item { Title(title = selectedInstallation?.name ?: "", fontSize = 30) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Title(
                        title = stringResource(
                            id = R.string.home_installation_power_subscribed,
                            selectedInstallation?.powerSubscribed ?: 0.0
                        ),
                        fontSize = 16,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                VerticalDivider(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Title(
                        title = stringResource(
                            id = R.string.home_installation_energy,
                            selectedInstallation?.energyWh?.div(1000.0) ?: 0.0
                        ),
                        fontSize = 16,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

            }
        }

        item {
            BannerUserInstallation(
                id = selectedInstallation?.type?.imageResId ?: 0
            )
        }

        item { HorizontalDivider(Modifier.fillMaxWidth().padding(vertical = 16.dp)) }

        item { Spacer(Modifier.height(16.dp)) }

        item { Title(stringResource(R.string.appliance_filter_type), 18) }

        item {
            InstallationTypeFilterByType(
                selectedType = selectedInstallationType,
                onTypeSelected = { selectedInstallationType = it }
            )
        }

        item {
            ApplianceHeatTypeFilter(
                title = stringResource(R.string.appliance_filter_heat_type),
                selectedHeatType = selectedHeatType,
                onHeatTypeSelected = { selectedHeatType = it }
            )
        }

        item { Title(stringResource(R.string.appliance_add_title), 18) }

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

            ApplianceRow(filteredCatalog, onApplianceClick)
        }

        item { HorizontalDivider(Modifier.fillMaxWidth().padding(vertical = 16.dp)) }

        item {
            when (applianceState) {
                is UiState.Loading -> CircularProgressIndicator()
                is UiState.Success -> {
                    val userAppliances =
                        (applianceState as UiState.Success<List<Appliance>>).data
                    UserAppliancesList(userAppliances, onApplianceClick)
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
                    onClick = { /* futur détail */ }
                )
            } else {
                Text(stringResource(R.string.no_circuit_found))
            }
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

}
