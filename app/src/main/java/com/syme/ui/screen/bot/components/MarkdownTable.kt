package com.syme.ui.screen.bot.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography

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

    Box(modifier = modifier.horizontalScroll(rememberScrollState())) {
        SubcomposeLayout { _ ->

            val density = this

            // ─── Passe 1 : mesure avec Markdown ───────────────────────────────
            val colWidths = IntArray(colCount)

            allRows.forEachIndexed { rowIdx, row ->
                row.forEachIndexed { colIdx, rawCell ->
                    if (colIdx >= colCount) return@forEachIndexed

                    val placeable = subcompose("measure-$rowIdx-$colIdx") {
                        Markdown(
                            content = rawCell,
                            colors = markdownColor(text = textColor),
                            typography = markdownTypography(
                                text = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = if (rowIdx == 0) FontWeight.Bold else FontWeight.Normal,
                                )
                            ),
                            imageTransformer = Coil3ImageTransformerImpl,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }.first().measure(Constraints())

                    if (placeable.width > colWidths[colIdx]) {
                        colWidths[colIdx] = placeable.width
                    }
                }
            }

            val totalWidth = colWidths.sum() + (colCount + 1) * 1.dp.roundToPx()
            var currentY = 0

            // ─── Passe 2 : rendu réel ─────────────────────────────────────────
            val placeables = allRows.mapIndexed { rowIdx, row ->
                val isHeader = rowIdx == 0

                val bg = when {
                    isHeader        -> headerBg
                    rowIdx % 2 == 0 -> rowBg
                    else            -> altRowBg
                }

                row.mapIndexed { colIdx, rawCell ->
                    if (colIdx >= colCount) return@mapIndexed null

                    subcompose("cell-$rowIdx-$colIdx") {
                        Box(
                            modifier = Modifier
                                .width(with(density) { colWidths[colIdx].toDp() })
                                .background(bg)
                                .border(0.5.dp, borderColor)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Markdown(
                                content = rawCell,
                                colors = markdownColor(
                                    text = textColor,
                                    codeText = textColor,
                                    codeBackground = textColor.copy(alpha = 0.10f),
                                    dividerColor = textColor.copy(alpha = 0.20f),
                                    linkText = MaterialTheme.colorScheme.primary,
                                    inlineCodeText = textColor,
                                    inlineCodeBackground = textColor.copy(alpha = 0.10f),
                                ),
                                typography = markdownTypography(
                                    text = TextStyle(
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp,
                                        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                                    )
                                ),
                                imageTransformer = Coil3ImageTransformerImpl
                            )
                        }
                    }.first().measure(
                        Constraints(
                            minWidth = colWidths[colIdx],
                            maxWidth = colWidths[colIdx]
                        )
                    )
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