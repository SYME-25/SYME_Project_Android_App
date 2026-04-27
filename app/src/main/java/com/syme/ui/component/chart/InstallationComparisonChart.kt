package com.syme.ui.component.chart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.domain.model.InstallationConsumptionEntry
import com.syme.ui.theme.Accent200
import com.syme.ui.theme.Accent500
import com.syme.ui.theme.AvatarFemaleFg
import com.syme.ui.theme.Brand300
import com.syme.ui.theme.Brand500
import com.syme.ui.theme.SemanticError100
import com.syme.ui.theme.SemanticError500
import com.syme.ui.theme.SemanticInfo100
import com.syme.ui.theme.SemanticInfo500
import com.syme.ui.theme.SemanticSuccess100
import com.syme.ui.theme.SemanticSuccess500
import com.syme.ui.theme.SemanticWarning100
import com.syme.ui.theme.SemanticWarning500
import com.syme.R
import kotlin.math.roundToInt

/**
 * Palette alignée sur le design system SYME.
 * Chaque paire [sombre, clair] est utilisée pour le gradient horizontal de la barre.
 *
 * Tokens existants réutilisés directement :
 *   Brand500/Brand300, Accent500/Accent200, SemanticWarning500/Warning100,
 *   SemanticError500/Error100, SemanticInfo500/Info100, SemanticSuccess500/Success100
 * Couleurs dérivées ajoutées (cohérentes avec la charte Tailwind interne) :
 *   Violet (#7C3AED → #C4B5FD), Rose (AvatarFemaleFg → #FBCFE8)
 */
private val installationColors = listOf(
    listOf(Brand500, Brand300),                      // Brand
    listOf(Accent500, Accent200),                    // Accent
    listOf(SemanticWarning500, SemanticWarning100),  // Warning
    listOf(SemanticError500, SemanticError100),      // Error
    listOf(SemanticInfo500, SemanticInfo100),        // Info
    listOf(SemanticSuccess500, SemanticSuccess100),  // Success
    listOf(Color(0xFF7C3AED), Color(0xFFC4B5FD)),    // Violet (Dérivé)
    listOf(AvatarFemaleFg, Color(0xFFFBCFE8)),       // Rose (AvatarFemale)
)

/**
 * Graphique en barres horizontales affichant la consommation totale
 * de chaque installation sur la même période, permettant une comparaison directe.
 *
 * Usage dans ConsumptionScreen :
 * ```
 * InstallationComparisonChart(
 *     entries = installationConsumptionEntries,
 *     modifier = Modifier.padding(horizontal = 16.dp)
 * )
 * ```
 */
@Composable
fun InstallationComparisonChart(
    entries: List<InstallationConsumptionEntry>,
    modifier: Modifier = Modifier,
    barHeight: Dp = 36.dp,
    animationDurationMs: Int = 800,
    valueFormatter: (Double) -> String = { v ->
        when {
            v >= 1_000_000 -> "${"%.1f".format(v / 1_000_000)} MWh"
            v >= 1_000     -> "${"%.1f".format(v / 1_000)} kWh"
            else           -> "${v.roundToInt()} Wh"
        }
    }
) {
    if (entries.isEmpty()) return

    val maxValue = entries.maxOf { it.totalEnergyWh }.coerceAtLeast(1.0)
    val sortedEntries = entries.sortedByDescending { it.totalEnergyWh }

    // Animation d'entrée : chaque barre s'anime individuellement
    val animatedFractions = sortedEntries.mapIndexed { index, _ ->
        remember { Animatable(0f) }.also { anim ->
            LaunchedEffect(sortedEntries[index].totalEnergyWh) {
                anim.snapTo(0f)
                anim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = animationDurationMs,
                        delayMillis = index * 80
                    )
                )
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── Titre de la section ──────────────────────────────────────────
        Text(
            text = stringResource(R.string.consumption_by_installation),
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        sortedEntries.forEachIndexed { index, entry ->
            val fraction = (entry.totalEnergyWh / maxValue).toFloat()
            val animFraction = animatedFractions[index].value * fraction
            val colorPair = installationColors[index % installationColors.size]
            val isTop = index == 0
            val isBottom = index == sortedEntries.lastIndex

            InstallationBarRow(
                entry = entry,
                animatedFraction = animFraction,
                barColor = colorPair,
                barHeight = barHeight,
                valueFormatter = valueFormatter,
                rank = index + 1,
                isTop = isTop,
                isLast = isBottom
            )
        }

        // ── Légende résumée ─────────────────────────────────────────────
        if (sortedEntries.size >= 2) {
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                thickness = 0.5.dp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // sortedEntries est trié sortedByDescending → first() = MAX, last() = MIN
                LegendDot(
                    color = installationColors[0][0],
                    label = "↑ max · ${sortedEntries.first().installationName.take(14)}"
                )
                LegendDot(
                    color = installationColors[sortedEntries.lastIndex % installationColors.size][0],
                    label = "↓ min · ${sortedEntries.last().installationName.take(14)}"
                )
            }
        }
    }
}

@Composable
private fun InstallationBarRow(
    entry: InstallationConsumptionEntry,
    animatedFraction: Float,
    barColor: List<Color>,
    barHeight: Dp,
    valueFormatter: (Double) -> String,
    rank: Int,
    isTop: Boolean,
    isLast: Boolean
) {
    val gradient = Brush.horizontalGradient(colors = barColor)
    // isTop  = consommation MAX (index 0 dans sortedByDescending) → accentué
    // isLast = consommation MIN → légèrement atténué
    val labelColor = when {
        isTop  -> barColor[0]
        isLast -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        else   -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(barHeight),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nom de l'installation (tronqué si trop long)
        Text(
            text = entry.installationName,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isTop) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp
            ),
            color = labelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(88.dp),
            textAlign = TextAlign.End
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Barre animée
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barWidth = size.width * animatedFraction
                if (barWidth > 0f) {
                    drawRoundRect(
                        brush = gradient,
                        topLeft = Offset(0f, size.height * 0.15f),
                        size = Size(barWidth, size.height * 0.7f),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                }
                // Track de fond — utilise outline du thème pour s'adapter light/dark
                drawRoundRect(
                    color = Color(0xFFCDD5E0).copy(alpha = 0.30f), // Neutral200
                    topLeft = Offset(0f, size.height * 0.15f),
                    size = Size(size.width, size.height * 0.7f),
                    cornerRadius = CornerRadius(12f, 12f)
                )
                // Redessiner la barre par-dessus le track
                if (barWidth > 0f) {
                    drawRoundRect(
                        brush = gradient,
                        topLeft = Offset(0f, size.height * 0.15f),
                        size = Size(barWidth, size.height * 0.7f),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Valeur numérique
        Text(
            text = valueFormatter(entry.totalEnergyWh),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isTop) FontWeight.Bold else FontWeight.Normal,
                fontSize = 10.sp
            ),
            color = if (isTop) barColor[0] else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(64.dp),
            textAlign = TextAlign.Start,
            maxLines = 1
        )
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}