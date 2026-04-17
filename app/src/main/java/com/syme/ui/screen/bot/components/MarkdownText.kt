package com.syme.ui.screen.bot.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography

// ─── Math / LaTeX pre-processing (garde ta logique existante) ────────────────
fun cleanMathText(text: String): String {
    var t = text

    t = t
        .replace("\\\\times", "×")
        .replace("\\\\cdot", "·")
        .replace("\\\\div", "÷")
        .replace("\\\\neq", "≠")
        .replace("\\\\leq", "≤")
        .replace("\\\\geq", "≥")
        .replace("\\\\approx", "≈")
        .replace("\\\\infty", "∞")
        .replace("\\\\pi", "π")
        .replace("\\\\alpha", "α")
        .replace("\\\\beta", "β")
        .replace("\\\\gamma", "γ")
        .replace("\\\\theta", "θ")
        .replace("\\\\lambda", "λ")
        .replace("\\\\mu", "μ")
        .replace("\\\\sigma", "σ")
        .replace("\\\\Delta", "Δ")
        .replace("\\\\sqrt", "√")
        .replace("\\\\rightarrow", "→")
        .replace("\\\\leftarrow", "←")
        .replace("\\\\leftrightarrow", "↔")
        .replace("\\\\degree", "°")
        .replace("\\\\%", "%")
        .replace("\\\\sum", "Σ")
        .replace("\\\\int", "∫")
        .replace("\\\\lim", "lim")
        .replace("\\\\in", "∈")
        .replace("\\\\forall", "∀")
        .replace("\\\\exists", "∃")

    // ─── FRACTION SAFE ─────────────────────────────────────
    val fracRegex = Regex("""\\\\frac\{([^{}]+)\}\{([^{}]+)\}""")

    t = fracRegex.replace(t) {
        "${it.groupValues[1]}/${it.groupValues[2]}"
    }

    // ─── COMMANDES TEXTE (SAFE ICU) ───────────────────────
    fun stripCommand(command: String, input: String): String {
        val regex = Regex("""\\\\$command\{([^{}]*)\}""")
        return regex.replace(input) { it.groupValues[1] }
    }

    t = stripCommand("text", t)
    t = stripCommand("mathrm", t)
    t = stripCommand("textrm", t)
    t = stripCommand("boxed", t)

    // ─── Nettoyage final ───────────────────────────────────
    t = t.replace("{", "").replace("}", "")

    return t
}

// ─── Public composable ───────────────────────────────────────────────────────

@Composable
fun MarkdownText(
    raw: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val segments = remember(raw) { parseMarkdownSegments(cleanMathText(raw)) }

    Column(modifier = modifier) {
        segments.forEach { segment ->
            if (segment.isTable) {
                MarkdownTable(
                    tableMarkdown = segment.content,
                    textColor = color,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Markdown(
                    content = segment.content,
                    modifier = Modifier.fillMaxWidth(),
                    colors = markdownColor(
                        text = color,
                        codeText = color,
                        codeBackground = color.copy(alpha = 0.10f),
                        dividerColor = color.copy(alpha = 0.20f),
                        linkText = MaterialTheme.colorScheme.primary,
                        inlineCodeText = color,
                        inlineCodeBackground = color.copy(alpha = 0.10f),
                    ),
                    typography = markdownTypography(
                        text = TextStyle(
                            fontSize = 16.sp,
                            lineHeight = 26.sp,
                            fontWeight = FontWeight.Normal,
                        ),
                        code = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                        ),
                        h1 = TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 36.sp,
                        ),
                        h2 = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 32.sp,
                        ),
                        h3 = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 28.sp,
                        ),
                        h4 = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 26.sp,
                        ),
                        h5 = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        h6 = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        quote = TextStyle(
                            fontSize = 15.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.Normal,
                        ),
                        paragraph = TextStyle(
                            fontSize = 16.sp,
                            lineHeight = 26.sp,
                        ),
                        ordered = TextStyle(fontSize = 16.sp, lineHeight = 26.sp),
                        bullet = TextStyle(fontSize = 16.sp, lineHeight = 26.sp),
                    ),
                    imageTransformer = Coil3ImageTransformerImpl,
                )
            }
        }
    }
}