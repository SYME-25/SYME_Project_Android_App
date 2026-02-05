package com.syme.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syme.data.session.SessionManager
import com.syme.ui.navigation.RootRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination

    private val _isSplashScreenVisible = MutableStateFlow(true)
    val isSplashScreenVisible: StateFlow<Boolean> = _isSplashScreenVisible.asStateFlow()

    init {
        viewModelScope.launch {
            delay(1500) // temps du splash
            _startDestination.value =
                if (sessionManager.isUserLoggedIn()) {
                    RootRoute.Main
                } else {
                    RootRoute.Auth
                }

            delay(2000)
            _isSplashScreenVisible.value = false
        }
    }
}
