// MarkdownTable.kt
package com.syme.ui.screen.bot.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownTable(
    tableMarkdown: String,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val (headers, rows) = remember(tableMarkdown) { parseTableData(tableMarkdown) }
    if (headers.isEmpty()) return

    val allRows = listOf(headers) + rows
    val colCount = headers.size

    val borderColor = MaterialTheme.colorScheme.outlineVariant
    val headerBg    = MaterialTheme.colorScheme.surfaceVariant
    val rowBg       = MaterialTheme.colorScheme.surface
    val altRowBg    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    // Mesure la largeur naturelle de chaque colonne puis rend le tableau
    Box(modifier = modifier.horizontalScroll(rememberScrollState())) {
        SubcomposeLayout { constraints ->
            // Passe 1 : mesure la largeur idéale de chaque cellule
            val colWidths = IntArray(colCount) { 0 }

            allRows.forEachIndexed { rowIdx, row ->
                row.forEachIndexed { colIdx, cell ->
                    if (colIdx >= colCount) return@forEachIndexed
                    val placeable = subcompose("measure-$rowIdx-$colIdx") {
                        Text(
                            text       = cell,
                            fontSize   = 14.sp,
                            fontWeight = if (rowIdx == 0) FontWeight.Bold else FontWeight.Normal,
                            modifier   = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }.first().measure(Constraints()) // unbounded
                    if (placeable.width > colWidths[colIdx]) {
                        colWidths[colIdx] = placeable.width
                    }
                }
            }

            val totalWidth  = colWidths.sum() + (colCount + 1) * 1.dp.roundToPx()
            var currentY    = 0

            // Passe 2 : rendu réel avec les largeurs fixées
            val placeables = allRows.mapIndexed { rowIdx, row ->
                val isHeader = rowIdx == 0
                val bg       = when {
                    isHeader       -> headerBg
                    rowIdx % 2 == 0 -> rowBg
                    else            -> altRowBg
                }
                row.mapIndexed { colIdx, cell ->
                    if (colIdx >= colCount) return@mapIndexed null
                    subcompose("cell-$rowIdx-$colIdx") {
                        Box(
                            modifier = Modifier
                                .width(colWidths[colIdx].toDp())
                                .background(bg)
                                .border(0.5.dp, borderColor)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text       = cell,
                                color      = textColor,
                                fontSize   = 14.sp,
                                fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                                lineHeight  = 20.sp,
                                softWrap   = true   // wrap dans la colonne si le texte est long
                            )
                        }
                    }.first().measure(Constraints(minWidth = colWidths[colIdx], maxWidth = colWidths[colIdx]))
                }
            }

            val totalHeight = placeables.sumOf { row ->
                row.filterNotNull().maxOfOrNull { it.height } ?: 0
            }

            layout(totalWidth, totalHeight) {
                placeables.forEach { row ->
                    var currentX = 0
                    val rowHeight = row.filterNotNull().maxOfOrNull { it.height } ?: 0
                    row.filterNotNull().forEach { placeable ->
                        placeable.placeRelative(currentX, currentY)
                        currentX += placeable.width
                    }
                    currentY += rowHeight
                }
            }
        }
    }
}