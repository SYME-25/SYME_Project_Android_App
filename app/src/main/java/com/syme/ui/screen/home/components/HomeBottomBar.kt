package com.syme.ui.screen.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.ui.navigation.main.MainRoute
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

@Composable
fun HomeBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val colorPrimary  = MaterialTheme.colorScheme.primary
    val colorInactive = MaterialTheme.colorScheme.onSurfaceVariant
    val colorPillBg   = MaterialTheme.colorScheme.primaryContainer
    val surfaceColor  = MaterialTheme.colorScheme.surface

    val items = listOf(
        MainRoute.HomeScreen,
        MainRoute.ConsumptionScreen,
        MainRoute.BillScreen,
        MainRoute.SettingsScreen
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp), // réduit
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(40.dp)) // légèrement réduit
                .hazeEffect(
                    state = hazeState,
                    style = HazeStyle(
                        backgroundColor = surfaceColor,
                        tint = HazeTint(surfaceColor.copy(alpha = 0.4f)),
                        blurRadius = 16.dp // réduit léger
                    )
                )
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.White.copy(alpha = 0.08f),
                            0.6f to Color.Transparent
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp), // réduit
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
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
                    val boxSize by animateDpAsState(
                        targetValue = if (selected) 38.dp else 34.dp, // réduit
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "boxSize"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onNavigate(screen.route) }
                            .padding(vertical = 4.dp) // réduit
                    ) {
                        Box(
                            modifier = Modifier
                                .size(boxSize)
                                .clip(RoundedCornerShape(12.dp)) // réduit
                                .background(
                                    if (selected)
                                        Brush.verticalGradient(
                                            listOf(
                                                colorPillBg.copy(alpha = 0.75f),
                                                colorPillBg.copy(alpha = 0.55f)
                                            )
                                        )
                                    else
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Transparent)
                                        )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(screen.icon),
                                contentDescription = screen.route,
                                tint = iconTint,
                                modifier = Modifier.size(18.dp) // réduit
                            )
                        }
                        Spacer(Modifier.height(2.dp)) // réduit
                        Text(
                            text = stringResource(screen.title),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 11.sp, // réduit
                            fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold,
                            color = labelColor
                        )
                    }
                }
            }
        }
    }
}