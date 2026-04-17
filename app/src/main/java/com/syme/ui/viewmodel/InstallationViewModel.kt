package com.syme.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.syme.domain.model.Installation
import com.syme.domain.state.UiState
import com.syme.data.repository.InstallationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InstallationViewModel @Inject constructor(
    private val repository: InstallationRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<Installation>>>(UiState.Idle)
    val state: StateFlow<UiState<List<Installation>>> = _state

    private val _selected = MutableStateFlow<Installation?>(null)
    val selected: StateFlow<Installation?> = _selected

    private var observeJob: Job? = null

    // 👁 OBSERVE USER INSTALLATIONS
    fun observe(ownerId: String) {
        if (ownerId.isBlank()) return

        // Vérifier que l'utilisateur est bien authentifié avant d'observer
        if (auth.currentUser == null) {
            Log.w("InstallationViewModel", "observe() appelé sans utilisateur authentifié")
            _state.value = UiState.Error("Non authentifié")
            return
        }

        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _state.value = UiState.Loading
            repository.observeAll(ownerId)
                .catch { e ->
                    // Capturer l'erreur Firestore (PERMISSION_DENIED, etc.)
                    // pour éviter le crash fatal
                    Log.e("InstallationViewModel", "Erreur observeAll", e)
                    _state.value = UiState.Error(e.message ?: "Erreur de lecture")
                }
                .collect { list ->
                    _state.value = UiState.Success(list)
                }
        }
    }

    fun getById(ownerId: String, id: String) = viewModelScope.launch {
        _state.value = UiState.Loading
        try {
            val item = repository.getById(ownerId, id)
            _selected.value = item
        } catch (e: Exception) {
            Log.e("InstallationViewModel", "Erreur getById", e)
            _state.value = UiState.Error(e.message ?: "Load failed")
        }
    }

    fun insert(ownerId: String, installation: Installation) = viewModelScope.launch {
        try {
            repository.insert(ownerId, installation)
        } catch (e: Exception) {
            Log.e("InstallationViewModel", "Erreur insert", e)
            _state.value = UiState.Error(e.message ?: "Insert failed")
        }
    }

    fun update(ownerId: String, installation: Installation) = viewModelScope.launch {
        try {
            repository.update(ownerId, installation)
        } catch (e: Exception) {
            Log.e("InstallationViewModel", "Erreur update", e)
            _state.value = UiState.Error(e.message ?: "Update failed")
        }
    }

    fun delete(ownerId: String, installation: Installation) = viewModelScope.launch {
        try {
            repository.delete(ownerId, installation.installationId)
        } catch (e: Exception) {
            Log.e("InstallationViewModel", "Erreur delete", e)
            _state.value = UiState.Error(e.message ?: "Delete failed")
        }
    }

    fun select(installation: Installation) {
        _selected.value = installation
    }

    fun clearSelected() {
        _selected.value = null
    }

    override fun onCleared() {
        super.onCleared()
        observeJob?.cancel()
    }
}