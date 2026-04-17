package com.syme.ui.navigation.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.syme.ui.navigation.extensions.navigateToApplianceDetail
import com.syme.ui.navigation.extensions.navigateToInstallationDetail
import com.syme.ui.navigation.extensions.navigateToUserInstallationDetail
import com.syme.ui.screen.appliance.ApplianceDetailScreen
import com.syme.ui.screen.bill.BillScreen
import com.syme.ui.screen.bot.BotScreen
import com.syme.ui.screen.consumption.ConsumptionScreen
import com.syme.ui.screen.home.HomeScreen
import com.syme.ui.screen.installation.InstallationDetailScreen
import com.syme.ui.screen.installation.UserInstallationDetailScreen
import com.syme.ui.screen.notification.NotificationsScreen
import com.syme.ui.screen.profile.ProfileScreen
import com.syme.ui.screen.settings.SettingsScreen
import com.syme.ui.viewmodel.ApplianceViewModel
import com.syme.ui.viewmodel.AuthViewModel
import com.syme.ui.viewmodel.BillViewModel
import com.syme.ui.viewmodel.BotViewModel
import com.syme.ui.viewmodel.CircuitViewModel
import com.syme.ui.viewmodel.ConsumptionViewModel
import com.syme.ui.viewmodel.InstallationViewModel
import com.syme.ui.viewmodel.MeterViewModel
import com.syme.ui.viewmodel.NotificationsViewModel
import com.syme.ui.viewmodel.SettingsViewModel
import com.syme.ui.viewmodel.UserViewModel


@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.mainNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    installationViewModel: InstallationViewModel,
    consumptionViewModel: ConsumptionViewModel,
    meterViewModel: MeterViewModel,
    applianceViewModel: ApplianceViewModel,
    circuitViewModel: CircuitViewModel,
    billViewModel: BillViewModel,
    botViewModel: BotViewModel,
    notificationsViewModel: NotificationsViewModel,
    settingsViewModel: SettingsViewModel,
    contentPadding: PaddingValues
    ) {

    composable (MainRoute.BillScreen.route) {
        BillScreen(
            installationViewModel = installationViewModel,
            billViewModel = billViewModel,
            contentPadding = contentPadding
        )
    }

    composable (MainRoute.ConsumptionScreen.route) {
        ConsumptionScreen(
            installationViewModel = installationViewModel,
            consumptionViewModel = consumptionViewModel,
            meterViewModel = meterViewModel,
            contentPadding = contentPadding
        )
    }

    composable (MainRoute.HomeScreen.route) {
        HomeScreen(
            installationViewModel = installationViewModel,
            onNavigateToInstallationDetail = { installation ->
                navController.navigateToInstallationDetail(installation)
            },
            onNavigateToUserInstallationDetail = { installation ->
                navController.navigateToUserInstallationDetail(installation)
            },
            contentPadding = contentPadding
        )
    }

    composable (MainRoute.ProfileScreen.route) {
        ProfileScreen(
            userViewModel = userViewModel,
            contentPadding = contentPadding)
    }

    composable(MainRoute.NotificationScreen.route) {
        NotificationsScreen(
            viewModel = notificationsViewModel,
            contentPadding = contentPadding
        )
    }

    composable (MainRoute.BotScreen.route) {
        BotScreen(
            viewModel = botViewModel,
            contentPadding = contentPadding
            )
    }

    composable (MainRoute.SettingsScreen.route) {
        SettingsScreen(
            settingsViewModel = settingsViewModel,
            authViewModel = authViewModel,
            contentPadding = contentPadding
        )
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
                contentPadding = contentPadding
            )
        }
    }

    composable(MainRoute.UserInstallationDetailScreen.route){ backStackEntry ->

        val installationId =
            backStackEntry.arguments?.getString("installationId")

        if (installationId == null) {
            navController.popBackStack()
        } else {
            UserInstallationDetailScreen(
                installationId = installationId,
                installationViewModel = installationViewModel,
                applianceViewModel = applianceViewModel,
                meterViewModel = meterViewModel,
                circuitViewModel = circuitViewModel,
                onApplianceClick = { appliance ->
                    navController.navigateToApplianceDetail(appliance, installationId)
                },
                contentPadding = contentPadding
            )
        }
    }

    composable(MainRoute.ApplianceDetailScreen.route) { backStackEntry ->

        val applianceId =
            backStackEntry.arguments?.getString("applianceId")

        val installationId =
            backStackEntry.arguments?.getString("installationId")

        if (applianceId == null) {
            navController.popBackStack()
        } else {
            ApplianceDetailScreen(
                applianceId = applianceId,
                installationId = installationId,
                circuitViewModel = circuitViewModel,
                applianceViewModel = applianceViewModel,
                onBack = { navController.popBackStack() },
                contentPadding = contentPadding
            )
        }
    }
}
