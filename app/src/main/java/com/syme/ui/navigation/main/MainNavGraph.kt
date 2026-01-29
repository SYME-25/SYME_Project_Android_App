package com.syme.ui.navigation.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.syme.ui.screen.analysis.AnalysisScreen
import com.syme.ui.screen.bill.BillScreen
import com.syme.ui.screen.consumption.ConsumptionScreen
import com.syme.ui.screen.home.HomeScreen
import com.syme.ui.screen.profile.ProfileScreen
import com.syme.ui.screen.settings.SettingsScreen

fun NavGraphBuilder.mainNavGraph(navController: NavHostController, paddingValues: PaddingValues) {

    composable (MainRoute.Analysis.route) {
        AnalysisScreen()
    }

    composable (MainRoute.Bill.route) {
        BillScreen()
    }

    composable (MainRoute.Consumption.route) {
        ConsumptionScreen()
    }

    composable (MainRoute.Home.route) {
        HomeScreen()
    }

    composable (MainRoute.Profile.route) {
        ProfileScreen()
    }

    composable (MainRoute.Settings.route) {
        SettingsScreen()
    }
}