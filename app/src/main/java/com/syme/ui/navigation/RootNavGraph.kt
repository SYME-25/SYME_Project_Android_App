package com.syme.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.syme.R
import com.syme.data.session.SessionManager
import com.syme.ui.component.compositionlocal.LocalCurrentUserSession
import com.syme.ui.navigation.auth.AuthRoute
import com.syme.ui.navigation.auth.authNavGraph
import com.syme.ui.navigation.main.MainRoute
import com.syme.ui.navigation.main.mainNavGraph
import com.syme.ui.screen.main.MainScreen
import com.syme.ui.snapshot.MessageType
import com.syme.ui.snapshot.globalMessageManager
import com.syme.ui.viewmodel.ApplianceViewModel
import com.syme.ui.viewmodel.AuthViewModel
import com.syme.ui.viewmodel.CircuitViewModel
import com.syme.ui.viewmodel.ConnectivityViewModel
import com.syme.ui.viewmodel.ConsumptionViewModel
import com.syme.ui.viewmodel.InstallationViewModel
import com.syme.ui.viewmodel.LoginViewModel
import com.syme.ui.viewmodel.MeasurementViewModel
import com.syme.ui.viewmodel.MeterViewModel
import com.syme.ui.viewmodel.RegisterViewModel
import com.syme.utils.connectivityFlow
import kotlinx.coroutines.delay


/**
 * RootNavGraph : centralise toutes les features de navigation
 *
 * Point d’entrée unique pour NavHost
 * - Auth
 * - Main (Home, Profile, etc.)
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RootNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    val installationViewModel: InstallationViewModel = hiltViewModel()
    val registerViewModel: RegisterViewModel = hiltViewModel()
    val loginViewModel: LoginViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    val consumptionViewModel: ConsumptionViewModel = hiltViewModel()
    val measurementViewModel: MeasurementViewModel = hiltViewModel()
    val session by authViewModel.currentSession.collectAsState()
    val connectivityViewModel: ConnectivityViewModel = hiltViewModel()
    val applianceViewModel : ApplianceViewModel = hiltViewModel()
    val meterViewModel: MeterViewModel = hiltViewModel()
    val circuitViewModel: CircuitViewModel = hiltViewModel()

    val isOnline by connectivityViewModel.isOnline.collectAsState()

    val noInternetMsg = stringResource(R.string.No_internet_connection)
    val connectionRestoredMsg = stringResource(R.string.Connection_restored)

    // 🌍 Écoute globale du réseau
    LaunchedEffect(isOnline) {
        if (!isOnline) {
            globalMessageManager.showMessage(
                type = MessageType.ERROR,
                isSystemMessage = true,
                customText = noInternetMsg,
                customIcon = Icons.Default.WifiOff
            )
        } else {
            globalMessageManager.showMessage(
                type = MessageType.SUCCESS,
                isSystemMessage = true,
                customText = connectionRestoredMsg,
                customIcon = Icons.Default.Wifi
            )
            delay(2500)
            if (globalMessageManager.message.value?.isSystemMessage == true) {
                globalMessageManager.clearMessage()
            }
        }
    }

    CompositionLocalProvider(
        LocalCurrentUserSession provides session
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            navigation(
                startDestination = AuthRoute.Login.route,
                route = RootRoute.Auth
            ) {
                authNavGraph(navController, registerViewModel, loginViewModel)
            }

            composable(MainRoute.MainScreen.route) {
                MainScreen(
                    installationViewModel = installationViewModel,
                    consumptionViewModel = consumptionViewModel,
                    measurementViewModel = measurementViewModel,
                    meterViewModel = meterViewModel,
                    applianceViewModel = applianceViewModel,
                    circuitViewModel = circuitViewModel
                )
            }
        }
    }
}
