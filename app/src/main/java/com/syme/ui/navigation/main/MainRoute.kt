package com.syme.ui.navigation.main

import com.syme.R

sealed class MainRoute(val route: String, val title: Int, val icon: Int) {
    object Home : MainRoute("home", R.string.bottom_title_home, R.drawable.outline_home_24)
    object Consumption : MainRoute("consumption", R.string.bottom_title_consumption, R.drawable.outline_bar_chart_24)
    object Analysis : MainRoute("analysis", R.string.bottom_title_analysis, R.drawable.outline_donut_large_24)
    object Bill : MainRoute("bill", R.string.bottom_title_bill, R.drawable.outline_energy_savings_leaf_24)
    object Settings : MainRoute("settings", R.string.bottom_title_settings, R.drawable.outline_settings_24)

    object MainScreen : MainRoute("main_container", 0, 0)

    object Profile : MainRoute("profile", 0, 0)
}
