package com.syme.ui.screen.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.ui.navigation.main.MainRoute

@Composable
fun HomeBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val colorPrimary  = Color(0xFF1A237E)
    val colorBg       = Color(0xFFF5F7FF)
    val colorInactive = Color(0xFF9FA8DA)

    val items = listOf(
        MainRoute.HomeScreen,
        MainRoute.ConsumptionScreen,
        MainRoute.AnalysisScreen,
        MainRoute.BillScreen,
        MainRoute.SettingsScreen
    )

    NavigationBar(
        containerColor = colorBg,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
    ) {
        items.forEach { screen ->
            val selected = currentRoute == screen.route

            val iconTint by animateColorAsState(
                targetValue = if (selected) colorPrimary else colorInactive,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "iconTint"
            )
            val labelColor by animateColorAsState(
                targetValue = if (selected) colorPrimary else colorInactive,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "labelColor"
            )

            NavigationBarItem(
                selected = selected,
                onClick  = { onNavigate(screen.route) },
                colors   = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                ),
                icon = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Pill indicator above icon
                        Box(
                            modifier = Modifier
                                .height(3.dp)
                                .width(if (selected) 24.dp else 0.dp)
                                .clip(CircleShape)
                                .background(colorPrimary)
                        )
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(if (selected) 40.dp else 36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selected) Color(0xFFDDE1F8) else Color.Transparent
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(screen.icon),
                                contentDescription = screen.route,
                                tint = iconTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                label = {
                    Text(
                        text = stringResource(screen.title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = labelColor
                    )
                }
            )
        }
    }
}