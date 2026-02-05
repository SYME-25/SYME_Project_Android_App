package com.syme.ui.navigation.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.syme.ui.navigation.extensions.navigateToInstallationDetail
import com.syme.ui.screen.analysis.AnalysisScreen
import com.syme.ui.screen.bill.BillScreen
import com.syme.ui.screen.consumption.ConsumptionScreen
import com.syme.ui.screen.home.HomeScreen
import com.syme.ui.screen.installation.InstallationDetailScreen
import com.syme.ui.screen.profile.ProfileScreen
import com.syme.ui.screen.settings.SettingsScreen
import com.syme.ui.viewmodel.ConsumptionViewModel
import com.syme.ui.viewmodel.InstallationViewModel
import com.syme.ui.viewmodel.MeasurementViewModel


@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.mainNavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    installationViewModel: InstallationViewModel,
    consumptionViewModel: ConsumptionViewModel,
    measurementViewModel: MeasurementViewModel
    ) {

    composable (MainRoute.AnalysisScreen.route) {
        AnalysisScreen()
    }

    composable (MainRoute.BillScreen.route) {
        BillScreen()
    }

    composable (MainRoute.ConsumptionScreen.route) {
        ConsumptionScreen(
            installationViewModel = installationViewModel,
            consumptionViewModel = consumptionViewModel,
            measurementViewModel = measurementViewModel
        )
    }

    composable (MainRoute.HomeScreen.route) {
        HomeScreen(
            installationViewModel = installationViewModel,
            ownerId = "1",
            onNavigateToInstallationDetail = { installation ->
                navController.navigateToInstallationDetail(installation)
            }
        )
    }

    composable (MainRoute.ProfileScreen.route) {
        ProfileScreen()
    }

    composable (MainRoute.SettingsScreen.route) {
        SettingsScreen()
    }

    composable(MainRoute.InstallationDetailScreen.route) { backStackEntry ->

        val installationId =
            backStackEntry.arguments?.getString("installationId")

        if (installationId == null) {
            navController.popBackStack()
        } else {
            InstallationDetailScreen(
                installationId = installationId,
                installationViewModel = installationViewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }

}