package com.syme.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syme.data.repository.NotificationRepository
import com.syme.domain.model.Notification
import com.syme.domain.model.enumeration.NotificationCategory
import com.syme.domain.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<Notification>>>(UiState.Idle)
    val state: StateFlow<UiState<List<Notification>>> = _state

    private val _selected = MutableStateFlow<Notification?>(null)
    val selected: StateFlow<Notification?> = _selected

    private val _selectedCategory = MutableStateFlow<NotificationCategory?>(null)
    val selectedCategory: StateFlow<NotificationCategory?> = _selectedCategory.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    val notifications: StateFlow<List<Notification>> = combine(
        _state,
        _selectedCategory
    ) { state, cat ->
        val all = (state as? UiState.Success)?.data ?: emptyList()
        if (cat == null) all else all.filter { it.category == cat }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private var observeJob: Job? = null
    private var unreadJob: Job? = null

    // ── Observe ───────────────────────────────────────────────────────────────

    fun observe(userId: String) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _state.value = UiState.Loading
            repository.observeAll(userId)
                .catch { e ->
                    Log.e("NotificationsViewModel", "observe failed", e)
                    _state.value = UiState.Error(e.message ?: "Observe failed")
                }
                .collect { list ->
                    _state.value = UiState.Success(list)
                }
        }

        unreadJob?.cancel()
        unreadJob = viewModelScope.launch {
            repository.observeUnreadCount(userId)
                .catch { Log.e("NotificationsViewModel", "unreadCount failed", it) }
                .collect { _unreadCount.value = it }
        }
    }

    // ── Filter ────────────────────────────────────────────────────────────────

    fun setFilter(category: NotificationCategory?) {
        _selectedCategory.value = category
    }

    // ── Selection ─────────────────────────────────────────────────────────────

    fun select(notification: Notification) {
        _selected.value = notification
    }

    fun clearSelected() {
        _selected.value = null
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    fun markAsRead(userId: String, notification: Notification) {
        if (notification.isRead) return
        viewModelScope.launch {
            repository.markAsRead(userId, notification.notificationId)
        }
    }

    fun markAllAsRead(userId: String) = viewModelScope.launch {
        repository.markAllAsRead(userId)
    }

    fun delete(userId: String, notificationId: String) = viewModelScope.launch {
        repository.delete(userId, notificationId)
    }

    fun deleteAll(userId: String) = viewModelScope.launch {
        repository.deleteAll(userId)
    }
}