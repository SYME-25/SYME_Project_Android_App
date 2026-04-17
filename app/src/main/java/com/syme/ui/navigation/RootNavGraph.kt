package com.syme.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.syme.R
import com.syme.domain.state.AuthState
import com.syme.ui.component.compositionlocal.LocalCurrentUserSession
import com.syme.ui.navigation.auth.AuthNavHost
import com.syme.ui.navigation.main.MainNavHost
import com.syme.ui.snapshot.MessageType
import com.syme.ui.snapshot.globalMessageManager
import com.syme.ui.viewmodel.AuthViewModel
import com.syme.ui.viewmodel.ConnectivityViewModel
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RootNavGraph(
    navController: NavHostController
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()

    val connectivityViewModel: ConnectivityViewModel = hiltViewModel()
    val isOnline by connectivityViewModel.isOnline.collectAsState()

    val noInternetMsg = stringResource(R.string.No_internet_connection)
    val connectionRestoredMsg = stringResource(R.string.Connection_restored)

    // 🌍 Réseau global
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

    when (authState) {

        is AuthState.Loading -> {}

        is AuthState.Unauthenticated -> {
            AuthNavHost(navController)
        }

        is AuthState.Authenticated -> {
            val user = (authState as AuthState.Authenticated).user

            CompositionLocalProvider(
                LocalCurrentUserSession provides user
            ) {
                MainNavHost(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }
        }
    }
}