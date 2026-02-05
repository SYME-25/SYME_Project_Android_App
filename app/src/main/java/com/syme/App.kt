package com.syme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.syme.ui.navigation.RootNavGraph
import com.syme.ui.viewmodel.SplashViewModel

@Composable
fun App(paddingValues: PaddingValues) {
    val splashViewModel: SplashViewModel = hiltViewModel()
    val startDestination by splashViewModel.startDestination.collectAsState()

    val navController = rememberNavController()

    startDestination?.let { destination ->
        RootNavGraph(
            navController = navController,
            startDestination = destination
        )
    }
}