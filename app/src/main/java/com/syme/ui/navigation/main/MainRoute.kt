package com.syme.ui.navigation.main

import com.syme.R

sealed class MainRoute(val route: String, val title: Int, val icon: Int) {
    object HomeScreen : MainRoute("home", R.string.bottom_title_home, R.drawable.outline_home_24)
    object ConsumptionScreen : MainRoute("consumption", R.string.bottom_title_planning, R.drawable.outline_bar_chart_24)
    object AnalysisScreen : MainRoute("analysis", R.string.bottom_title_analysis, R.drawable.outline_donut_large_24)
    object BillScreen : MainRoute("bill", R.string.bottom_title_bill, R.drawable.outline_energy_savings_leaf_24)
    object SettingsScreen : MainRoute("settings", R.string.bottom_title_settings, R.drawable.outline_settings_24)

    object MainScreen : MainRoute("main_container", 0, 0)

    object ProfileScreen : MainRoute("profile", 0, 0)

    object InstallationDetailScreen : MainRoute("installationDetail/{installationId}", 0, 0) {
        fun createRoute(installationId: String) =
            "installationDetail/$installationId"
    }

}
