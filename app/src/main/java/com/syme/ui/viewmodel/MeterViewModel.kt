package com.syme.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syme.R
import com.syme.data.remote.repository.MeasurementRepository
import com.syme.data.remote.repository.MeterRepository
import com.syme.domain.model.Measurement
import com.syme.domain.model.Meter
import com.syme.domain.model.MeterEvent
import com.syme.domain.model.Relay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.security.MessageDigest
import javax.inject.Inject

@HiltViewModel
class MeterViewModel @Inject constructor(
    private val meterRepository: MeterRepository,
    private val measurementRepository: MeasurementRepository
) : ViewModel() {

    companion object {
        private const val TAG = "MeterViewModel"
    }

    // -------------------------
    // STATES
    // -------------------------
    private val _meters = MutableStateFlow<List<Meter>>(emptyList())
    val meters: StateFlow<List<Meter>> = _meters.asStateFlow()

    private val _measurements = MutableStateFlow<List<Measurement>>(emptyList())
    val measurements: StateFlow<List<Measurement>> = _measurements.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // 🎯 EVENTS (messages UI)
    private val _meterEvent = MutableSharedFlow<MeterEvent>()
    val meterEvent: SharedFlow<MeterEvent> = _meterEvent.asSharedFlow()

    // ⭐ Job pour éviter les collectors multiples
    private var measurementJob: Job? = null

    // -------------------------
    // OBSERVE METERS
    // -------------------------
    fun observeMeters(userId: String, installationId: String) {
        Log.d(TAG, "observeMeters() called with userId=$userId, installationId=$installationId")

        viewModelScope.launch {
            try {
                meterRepository.observeMeters(userId, installationId)
                    .collect { list ->
                        Log.d(TAG, "observeMeters() -> received ${list.size} meters")
                        _meters.value = list
                    }
            } catch (e: Exception) {
                Log.e(TAG, "observeMeters() error", e)
                _error.value = e.message
            }
        }
    }

    // -------------------------
    // HASH FUNCTION (SHA-256)
    // -------------------------
    private fun hashSecurityCode(code: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(code.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    // -------------------------
    // ADD / LOAD METER
    // -------------------------
    fun loadMeter(
        userId: String,
        installationId: String,
        meterId: String,
        securityCode: String
    ) {
        Log.d(TAG, "loadMeter() called with meterId=$meterId, installationId=$installationId")

        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            try {
                val hashedCode = hashSecurityCode(securityCode)

                val meter = meterRepository.loadMeterToInstallation(
                    userId = userId,
                    installationId = installationId,
                    meterId = meterId,
                    inputCodeHash = hashedCode
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
                Log.e(TAG, "Exception in loadMeter()", e)
                _meterEvent.emit(
                    MeterEvent.Error(
                        R.string.meter_error_loading_meters,
                        e.message ?: "Unknown error"
                    )
                )
            } finally {
                _loading.value = false
            }
        }
    }

    // -------------------------
    // OBSERVE MEASUREMENTS (corrigé)
    // -------------------------
    fun observeMeasurements(userId: String, installationId: String, meterId: String) {
        Log.d(TAG, "observeMeasurements() userId=$userId installationId=$installationId meterId=$meterId")

        // 🧹 Annule l'ancien collector s'il existe
        measurementJob?.cancel()

        measurementJob = viewModelScope.launch {
            try {
                measurementRepository
                    .observeRealtime(userId, installationId, meterId)
                    .collect { list ->
                        Log.d(TAG, "observeMeasurements() -> received ${list.size} measurements")
                        _measurements.value = list
                    }
            } catch (e: Exception) {
                Log.e(TAG, "observeMeasurements() error", e)
                _error.value = e.message
            }
        }
    }

    // -------------------------
    // RELAYS
    // -------------------------
    fun toggleRelay(userId: String, installationId: String, relay: Relay) {
        Log.d(
            TAG,
            "toggleRelay() relayId=${relay.relayId}, meterId=${relay.meterId}, currentState=${relay.currentState}"
        )

        viewModelScope.launch {
            try {
                val newState = if (relay.currentState == "ON") "OFF" else "ON"

                meterRepository.updateRelayState(
                    userId = userId,
                    installationId = installationId,
                    meterId = relay.meterId,
                    relayId = relay.relayId,
                    newState = newState
                )

            } catch (e: Exception) {
                Log.e(TAG, "toggleRelay() error", e)
                _error.value = e.message ?: "Failed to toggle relay"
            }
        }
    }
}
