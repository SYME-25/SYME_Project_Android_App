// TableParser.kt
package com.syme.ui.screen.bot.components

import com.syme.domain.model.MarkdownSegment

fun parseMarkdownSegments(raw: String): List<MarkdownSegment> {
    val lines = raw.lines()
    val segments = mutableListOf<MarkdownSegment>()
    val buffer = mutableListOf<String>()
    var inTable = false

    fun isTableRow(line: String) = line.trim().startsWith("|") && line.trim().endsWith("|")

    for (line in lines) {
        if (isTableRow(line)) {
            if (!inTable) {
                // flush text buffer
                if (buffer.isNotEmpty()) {
                    segments += MarkdownSegment(buffer.joinToString("\n"), isTable = false)
                    buffer.clear()
                }
                inTable = true
            }
            buffer += line
        } else {
            if (inTable) {
                segments += MarkdownSegment(buffer.joinToString("\n"), isTable = true)
                buffer.clear()
                inTable = false
            }
            buffer += line
        }
    }

    if (buffer.isNotEmpty()) {
        segments += MarkdownSegment(buffer.joinToString("\n"), isTable = inTable)
    }

    return segments
}

fun parseTableData(tableMarkdown: String): Pair<List<String>, List<List<String>>> {
    val rows = tableMarkdown.lines()
        .filter { it.trim().startsWith("|") }
        .filter { line ->
            // exclure les séparateurs ---
            !line.replace("|", "").replace("-", "").replace(":", "").replace(" ", "").isEmpty()
        }

    if (rows.isEmpty()) return Pair(emptyList(), emptyList())

    fun parseRow(row: String): List<String> =
        row.trim().removePrefix("|").removeSuffix("|")
            .split("|")
            .map { it.trim() }

    val headers = parseRow(rows.first())
    val dataRows = rows.drop(1).map { parseRow(it) }

    return Pair(headers, dataRows)
}