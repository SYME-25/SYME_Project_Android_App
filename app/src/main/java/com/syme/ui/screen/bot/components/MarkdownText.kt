package com.syme.ui.screen.bot.components

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.syme.domain.model.Token

/**
 * Renders a subset of Markdown as AnnotatedString.
 *
 * Supported:
 *   # / ## / ###         headings
 *   **bold**             bold
 *   *italic*             italic
 *   `code`               inline code
 *   ~~strikethrough~~    strikethrough
 *   - / * item           bullet list
 *   1. item              numbered list
 *
 * Robustness: unmatched opening markers (common during streaming) are
 * emitted as plain text — the user never sees raw ** or * characters.
 */
@Composable
fun MarkdownText(
    raw: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val annotated = buildAnnotatedString {
        val lines = raw.lines()
        lines.forEachIndexed { idx, line ->
            renderLine(line, color)
            if (idx < lines.lastIndex) append("\n")
        }
    }
    BasicText(text = annotated, modifier = modifier)
}

// ─── Line-level dispatcher ────────────────────────────────────────────────────

private fun AnnotatedString.Builder.renderLine(line: String, color: Color) {
    when {
        line.startsWith("### ") -> heading(line.removePrefix("### "), color, 17.sp, FontWeight.Bold)
        line.startsWith("## ")  -> heading(line.removePrefix("## "),  color, 19.sp, FontWeight.Bold)
        line.startsWith("# ")   -> heading(line.removePrefix("# "),   color, 21.sp, FontWeight.ExtraBold)

        line.startsWith("- ") || line.startsWith("* ") -> {
            withStyle(SpanStyle(color = color)) { append("• ") }
            renderInline(line.substring(2), color)
        }

        line.matches(Regex("^\\d+\\.\\s.*")) -> {
            val dot = line.indexOf(". ")
            withStyle(SpanStyle(color = color)) { append(line.substring(0, dot + 2)) }
            renderInline(line.substring(dot + 2), color)
        }

        line.isBlank() -> { /* keep as empty line */ }

        else -> renderInline(line, color)
    }
}

private fun AnnotatedString.Builder.heading(
    text: String, color: Color, size: TextUnit, weight: FontWeight
) = withStyle(SpanStyle(fontWeight = weight, fontSize = size, color = color)) { append(text) }

/**
 * Splits [text] into tokens. Any marker that is opened but never closed
 * (e.g. "**word" at the end of a streaming chunk) is returned as a Plain
 * token that includes the marker characters, so nothing raw leaks through.
 */
private fun tokenise(text: String): List<Token> {
    val result = mutableListOf<Token>()
    var i = 0

    while (i < text.length) {
        when {
            // ── **bold** ──────────────────────────────────────────────────────
            text.startsWith("**", i) -> {
                val end = text.indexOf("**", i + 2)
                if (end < 0) { result += Token.Plain(text.substring(i)); break }
                result += Token.Bold(text.substring(i + 2, end)); i = end + 2
            }
            // ── *italic*  (must not be followed by another * — that would be **) ─
            text[i] == '*' && text.getOrNull(i + 1) != '*' -> {
                val end = text.indexOf('*', i + 1)
                if (end < 0) { result += Token.Plain(text.substring(i)); break }
                result += Token.Italic(text.substring(i + 1, end)); i = end + 1
            }
            // ── `code` ────────────────────────────────────────────────────────
            text[i] == '`' -> {
                val end = text.indexOf('`', i + 1)
                if (end < 0) { result += Token.Plain(text.substring(i)); break }
                result += Token.Code(text.substring(i + 1, end)); i = end + 1
            }
            // ── ~~strike~~ ────────────────────────────────────────────────────
            text.startsWith("~~", i) -> {
                val end = text.indexOf("~~", i + 2)
                if (end < 0) { result += Token.Plain(text.substring(i)); break }
                result += Token.Strike(text.substring(i + 2, end)); i = end + 2
            }
            // ── plain text until the next marker ─────────────────────────────
            else -> {
                val next = (i until text.length).firstOrNull { j ->
                    text[j] == '*' || text[j] == '`' || text.startsWith("~~", j)
                } ?: text.length
                result += Token.Plain(text.substring(i, next)); i = next
            }
        }
    }
    return result
}

private fun AnnotatedString.Builder.renderInline(text: String, color: Color) {
    tokenise(text).forEach { token ->
        when (token) {
            is Token.Plain  -> withStyle(SpanStyle(color = color)) { append(token.t) }
            is Token.Bold   -> withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = color)) { append(token.t) }
            is Token.Italic -> withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = color)) { append(token.t) }
            is Token.Code   -> withStyle(SpanStyle(
                fontFamily = FontFamily.Monospace,
                background = color.copy(alpha = 0.12f),
                color = color,
                fontSize = 13.sp
            )) { append(token.t) }
            is Token.Strike -> withStyle(SpanStyle(
                color = color.copy(alpha = 0.6f),
                textDecoration = TextDecoration.LineThrough
            )) { append(token.t) }
        }
    }
}