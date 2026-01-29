package com.syme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.syme.ui.navigation.RootNavGraph

@Composable
fun App(paddingValues: PaddingValues) {
    val navController = rememberNavController()

    RootNavGraph(navController = navController, paddingValues = paddingValues)
}