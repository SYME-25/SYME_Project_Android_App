package com.syme.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syme.data.remote.repository.MeasurementRepository
import com.syme.domain.model.Measurement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MeasurementViewModel @Inject constructor(
    private val repository: MeasurementRepository
) : ViewModel() {

    private val _measurements = MutableStateFlow<List<Measurement>>(emptyList())
    val measurements: StateFlow<List<Measurement>> = _measurements

    fun observeRealtime(userId: String, installationId: String, meterId: String) {
        viewModelScope.launch {
            repository.observeRealtime(userId, installationId, meterId)
                .collect { list -> _measurements.value = list }
        }
    }

    fun loadHistorical(userId: String, installationId: String, meterId: String, limit: Int = 100) {
        viewModelScope.launch {
            _measurements.value = repository.getHistorical(userId, installationId, meterId, limit)
        }
    }

}
