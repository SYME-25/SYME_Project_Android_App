package com.syme.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syme.data.remote.repository.ConsumptionRepository
import com.syme.domain.model.Consumption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConsumptionViewModel @Inject constructor(
    private val repository: ConsumptionRepository
) : ViewModel() {

    private val _consumptions = MutableStateFlow<List<Consumption>>(emptyList())
    val consumptions: StateFlow<List<Consumption>> = _consumptions

    fun observeAll(userId: String) {
        viewModelScope.launch {
            repository.observeAll(userId).collect { list ->
                _consumptions.value = list
            }
        }
    }

    fun addConsumption(userId: String, consumption: Consumption) {
        viewModelScope.launch {
            repository.insert(userId, consumption)
        }
    }

    fun updateConsumption(userId: String, consumption: Consumption) {
        viewModelScope.launch {
            repository.update(userId, consumption)
        }
    }

    fun deleteConsumption(userId: String, consumptionId: String) {
        viewModelScope.launch {
            repository.delete(userId, consumptionId)
        }
    }
}
