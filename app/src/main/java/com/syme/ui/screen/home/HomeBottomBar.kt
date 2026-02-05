package com.syme.ui.screen.home

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.syme.ui.navigation.main.MainRoute

@Composable
fun HomeBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        MainRoute.HomeScreen,
        MainRoute.ConsumptionScreen,
        MainRoute.AnalysisScreen,
        MainRoute.BillScreen,
        MainRoute.SettingsScreen
    )

    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = { onNavigate(screen.route) },
                icon = {
                    Icon(
                        painter = painterResource(screen.icon),
                        contentDescription = screen.route
                    )
                },
                label = {
                    Text(
                        text = stringResource(screen.title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            )
        }
    }
}
