package com.syme.ui.screen.home

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.R
import com.syme.ui.navigation.main.MainRoute
import com.syme.ui.theme.SYMETheme

@Composable
fun HomeBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        MainRoute.Home,
        MainRoute.Consumption,
        MainRoute.Analysis,
        MainRoute.Bill,
        MainRoute.Settings
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
                    Text(stringResource(screen.title))
                }
            )
        }
    }
}
