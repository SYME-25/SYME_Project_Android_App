package com.syme.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syme.domain.model.Appliance
import com.syme.ui.state.UiState
import com.syme.data.remote.repository.ApplianceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApplianceViewModel @Inject constructor(
    private val repository: ApplianceRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<Appliance>>>(UiState.Idle)
    val state: StateFlow<UiState<List<Appliance>>> = _state

    private val _selected = MutableStateFlow<Appliance?>(null)
    val selected: StateFlow<Appliance?> = _selected

    private var observeJob: Job? = null

    fun observe(ownerId: String, installationId: String) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _state.value = UiState.Loading
            repository.observeAll(ownerId, installationId).collect { list ->
                _state.value = UiState.Success(list)
            }
        }
    }

    fun getById(ownerId: String, installationId: String, id: String) = viewModelScope.launch {
        _state.value = UiState.Loading
        try {
            val item = repository.getById(ownerId, installationId, id)
            _selected.value = item
        } catch (e: Exception) {
            _state.value = UiState.Error(e.message ?: "Load failed")
        }
    }

    fun insert(ownerId: String, installationId: String, appliance: Appliance) =
        viewModelScope.launch {
            try {
                repository.insert(ownerId, installationId, appliance)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Insert failed")
            }
        }

    fun update(ownerId: String, installationId: String, appliance: Appliance) =
        viewModelScope.launch {
            try {
                repository.update(ownerId, installationId, appliance)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Update failed")
            }
        }

    fun delete(ownerId: String, installationId: String, appliance: Appliance) =
        viewModelScope.launch {
            try {
                repository.delete(ownerId, installationId, appliance.applianceId)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Delete failed")
            }
        }

    fun select(appliance: Appliance) {
        _selected.value = appliance
    }

    fun clearSelected() {
        _selected.value = null
    }
}
