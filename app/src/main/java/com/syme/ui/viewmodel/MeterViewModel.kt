package com.syme.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syme.R
import com.syme.data.remote.repository.MeterRepository
import com.syme.domain.model.Measurement
import com.syme.domain.model.Meter
import com.syme.domain.model.MeterEvent
import com.syme.domain.model.Relay
import com.syme.utils.TimeUtils.startOfDayMillis
import com.syme.utils.round2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.security.MessageDigest
import javax.inject.Inject

@HiltViewModel
class MeterViewModel @Inject constructor(
    private val meterRepository: MeterRepository,
) : ViewModel() {

    // -------------------------
    // STATES
    // -------------------------
    private val _meters = MutableStateFlow<List<Meter>>(emptyList())
    val meters: StateFlow<List<Meter>> = _meters.asStateFlow()

    private val _measurements = MutableStateFlow<List<Measurement>>(emptyList())
    val measurements: StateFlow<List<Measurement>> = _measurements.asStateFlow()

    private val _aggregatedMeasurements = MutableStateFlow<List<Measurement>>(emptyList())
    val aggregatedMeasurements: StateFlow<List<Measurement>> = _aggregatedMeasurements

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _meterEvent = MutableSharedFlow<MeterEvent>()
    val meterEvent: SharedFlow<MeterEvent> = _meterEvent.asSharedFlow()

    // -------------------------
    // INTERNAL JOBS
    // -------------------------
    private var realtimeJob: Job? = null
    private var aggregationJob: Job? = null
    private val relayJobs = mutableMapOf<String, Job>()
    private val buffer = mutableListOf<Measurement>()
    private var lastTimestampSeen: Long = 0

    // -------------------------
    // OBSERVE METERS
    // -------------------------
    fun observeMeters(userId: String, installationId: String) {
        viewModelScope.launch {
            meterRepository.observeMeters(userId, installationId)
                .collect { _meters.value = it }
        }
    }

    // ----------------------------------------
    // OBSERVE MEASUREMENTS AGGREGATED
    // ----------------------------------------
    fun observeAggregatedMeasurements(
        userId: String,
        installationId: String
    ) {
        viewModelScope.launch {
            meterRepository
                .observeAggregatedMeasurementsFromFirestore(userId, installationId)
                .collect { list ->
                    _aggregatedMeasurements.value = list
                }
        }
    }

    // -------------------------
    // START REALTIME + AGGREGATION
    // -------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    fun startRealtimeAggregation(
        userId: String,
        installationId: String,
        meterId: String
    ) {
        stopRealtime()
        buffer.clear()
        lastTimestampSeen = System.currentTimeMillis()

        // 🔁 REALTIME DATABASE LISTENER
        realtimeJob = viewModelScope.launch {
            meterRepository
                .observeRealtimeFromRealtimeDb(userId, installationId, meterId)
                .collect { list ->
                    _measurements.value = list
                    val todayStart = startOfDayMillis()
                    val newOnes = list.filter {
                        it.timestamp > lastTimestampSeen && it.timestamp >= todayStart
                    }
                    if (newOnes.isNotEmpty()) {
                        lastTimestampSeen = newOnes.maxOf { it.timestamp }
                        buffer.addAll(newOnes)
                    }
                }
        }

        // ⏱️ AGGREGATION LOOP
        aggregationJob = viewModelScope.launch {
            while (true) {
                delay(60_000)
                val measurementToSave =
                    if (buffer.isNotEmpty()) {
                        aggregate(buffer, meterId, installationId)
                    } else {
                        defaultMeasurement(meterId, installationId)
                    }
                meterRepository.saveToFirestore(
                    userId,
                    installationId,
                    meterId,
                    measurementToSave
                )
                buffer.clear()
                lastTimestampSeen = System.currentTimeMillis()
            }
        }
    }

    fun stopRealtime() {
        realtimeJob?.cancel()
        aggregationJob?.cancel()
        buffer.clear()
        _measurements.value = emptyList()
    }

    // -------------------------
    // AGGREGATION
    // -------------------------
    private fun aggregate(
        list: List<Measurement>,
        meterId: String,
        installationId: String
    ): Measurement {
        fun avg(values: List<Double?>): Double? {
            val valid = values.filterNotNull()
            return if (valid.isNotEmpty()) valid.average() else null
        }
        return Measurement(
            timestamp = System.currentTimeMillis(),
            meterId = meterId,
            installationId = installationId,
            voltage = round2(avg(list.map { it.voltage })),
            current = round2(avg(list.map { it.current })),
            activePowerW = round2(avg(list.map { it.activePowerW })),
            reactivePowerVar = round2(avg(list.map { it.reactivePowerVar })),
            apparentPowerVA = round2(avg(list.map { it.apparentPowerVA })),
            energyActiveWh = round2(list.mapNotNull { it.energyActiveWh }.sum()),
            energyReactiveVarh = round2(list.mapNotNull { it.energyReactiveVarh }.sum()),
            energyApparentVAh = round2(list.mapNotNull { it.energyApparentVAh }.sum()),
            aiAnalysisStatus = "processed"
        )
    }

    private fun defaultMeasurement(
        meterId: String,
        installationId: String
    ): Measurement =
        Measurement(
            timestamp = System.currentTimeMillis(),
            meterId = meterId,
            installationId = installationId,
            voltage = 0.0,
            current = 0.0,
            activePowerW = 0.0,
            reactivePowerVar = 0.0,
            apparentPowerVA = 0.0,
            energyActiveWh = 0.0,
            energyReactiveVarh = 0.0,
            energyApparentVAh = 0.0,
            aiAnalysisStatus = "missing"
        )

    // -------------------------
    // LOAD METER
    // -------------------------
    fun loadMeter(
        userId: String,
        installationId: String,
        meterId: String,
        securityCode: String
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val hashedCode = hashSecurityCode(securityCode)
                val meter = meterRepository.loadMeterToInstallation(
                    userId,
                    installationId,
                    meterId,
                    hashedCode
                )
                if (meter != null) {
                    _meterEvent.emit(MeterEvent.Success(R.string.meter_add_success))
                } else {
                    _meterEvent.emit(
                        MeterEvent.Error(
                            R.string.meter_error_loading_meters,
                            "Invalid code or already used"
                        )
                    )
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    private fun hashSecurityCode(code: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(code.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    // -------------------------
    // RELAYS
    // -------------------------
    fun toggleRelay(
        userId: String,
        installationId: String,
        relay: Relay
    ) {
        viewModelScope.launch {
            try {
                val newState = if (relay.currentState == "ON") "OFF" else "ON"
                meterRepository.updateRelayState(
                    userId,
                    installationId,
                    relay.meterId,
                    relay.relayId,
                    newState
                )
                // L'UI se met à jour via observeRelaysForMeter()
            } catch (e: Exception) {
                _error.value = "Failed to update relay: ${e.message}"
            }
        }
    }

    fun observeRelaysForMeter(
        userId: String,
        installationId: String,
        meterId: String
    ) {
        relayJobs[meterId]?.cancel()
        relayJobs[meterId] = viewModelScope.launch {
            meterRepository.observeRelaysFromRealtimeDb(userId, installationId, meterId)
                .collect { realtimeRelays ->
                    _meters.update { meters ->
                        meters.map { meter ->
                            if (meter.meterId != meterId) return@map meter
                            meter.copy(
                                relays = meter.relays.map { relay ->
                                    val rtState = realtimeRelays
                                        .find { it.relayId == relay.relayId }
                                        ?.currentState
                                    if (rtState != null) relay.copy(currentState = rtState)
                                    else relay
                                }
                            )
                        }
                    }
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startBackgroundAggregationIfNeeded(
        userId: String,
        installationId: String,
        meterId: String
    ) {
        if (realtimeJob?.isActive == true) return
        startRealtimeAggregation(userId, installationId, meterId)
    }

    override fun onCleared() {
        super.onCleared()
        relayJobs.values.forEach { it.cancel() }
    }
}