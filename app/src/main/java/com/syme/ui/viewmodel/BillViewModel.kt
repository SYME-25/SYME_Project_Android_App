package com.syme.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syme.data.remote.repository.BillRepository
import com.syme.domain.model.Bill
import com.syme.utils.BillPdfGenerator
import com.syme.utils.EmailSender
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BillViewModel @Inject constructor(
    private val repository: BillRepository
) : ViewModel() {

    private val _bills = MutableStateFlow<List<Bill>>(emptyList())
    val bills: StateFlow<List<Bill>> = _bills.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun observeBills(
        userId: String,
        installationId: String
    ) {
        viewModelScope.launch {
            repository.observeBills(userId, installationId)
                .collect {
                    _bills.value = it
                }
        }
    }

    fun addBill(
        userId: String,
        installationId: String,
        bill: Bill
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.addBill(userId, installationId, bill)
            _isLoading.value = false
        }
    }

    fun updateBill(
        userId: String,
        installationId: String,
        bill: Bill
    ) {
        viewModelScope.launch {
            repository.updateBill(userId, installationId, bill)
        }
    }

    fun deleteBill(
        userId: String,
        installationId: String,
        billId: String
    ) {
        viewModelScope.launch {
            repository.deleteBill(userId, installationId, billId)
        }
    }

    fun exportBill(
        context: Context,
        bill: Bill,
        email: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {

            try {
                _isLoading.value = true

                val pdf = withContext(Dispatchers.IO) {
                    BillPdfGenerator.generate(context, bill)
                }

                EmailSender.send(context, pdf, email, bill.billId)

                onResult(true)

            } catch (e: Exception) {
                Log.d("BillViewModel", "exportBill: ${e.message}")
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }
}