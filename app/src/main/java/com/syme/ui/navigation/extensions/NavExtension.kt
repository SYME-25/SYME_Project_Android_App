package com.syme.ui.navigation.extensions

import androidx.navigation.NavController
import com.syme.domain.model.Appliance
import com.syme.domain.model.Installation
import com.syme.ui.navigation.main.MainRoute

fun NavController.navigateToInstallationDetail(installation: Installation) {
    navigate(
        MainRoute.InstallationDetailScreen.createRoute(
            installation.installationId
        )
    )
}

fun NavController.navigateToUserInstallationDetail(installation: Installation) {
    navigate(
        MainRoute.UserInstallationDetailScreen.createRoute(
            installation.installationId
        )
    )
}

fun NavController.navigateToApplianceDetail(appliance: Appliance, installationId: String) {
    navigate(
        MainRoute.ApplianceDetailScreen.createRoute(
            appliance.applianceId, installationId
        )
    )
}