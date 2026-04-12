package com.syme.ui.screen.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

@Composable
fun HomeBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(50.dp))
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to surfaceColor.copy(alpha = 0.78f), // ✅ Plus opaque = moins de transparence agressive
                            1.0f to surfaceColor.copy(alpha = 0.62f)
                        )
                    )
                )
        ) {
            // Shimmer subtil en haut
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(50.dp))
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.White.copy(alpha = 0.10f), // ✅ 0.18 → 0.10
                                0.6f to Color.Transparent
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
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
                        targetValue = if (selected) 42.dp else 38.dp,
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
                            .padding(vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(boxSize)
                                .clip(RoundedCornerShape(14.dp))
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
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = stringResource(screen.title),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold,
                            color = labelColor
                        )
                    }
                }
            }
        }
    }
}