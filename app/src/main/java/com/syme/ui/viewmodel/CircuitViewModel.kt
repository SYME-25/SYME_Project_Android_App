package com.syme.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syme.data.remote.repository.CircuitRepository
import com.syme.domain.model.Circuit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CircuitViewModel @Inject constructor(
    private val repository: CircuitRepository
) : ViewModel() {

    private val _circuits = MutableStateFlow<List<Circuit>>(emptyList())
    val circuits: StateFlow<List<Circuit>> = _circuits.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun observeCircuits(
        userId: String,
        installationId: String
    ) {
        viewModelScope.launch {
            repository.observeCircuits(userId, installationId)
                .collect {
                    _circuits.value = it
                }
        }
    }

    fun addCircuit(
        userId: String,
        installationId: String,
        circuit: Circuit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.addCircuit(userId, installationId, circuit)
            _isLoading.value = false
        }
    }

    fun updateCircuit(
        userId: String,
        installationId: String,
        circuit: Circuit
    ) {
        viewModelScope.launch {
            repository.updateCircuit(userId, installationId, circuit)
        }
    }

    fun deleteCircuit(
        userId: String,
        installationId: String,
        circuitId: String
    ) {
        viewModelScope.launch {
            repository.deleteCircuit(userId, installationId, circuitId)
        }
    }
}
