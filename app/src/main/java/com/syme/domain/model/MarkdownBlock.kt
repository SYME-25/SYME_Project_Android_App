package com.syme.domain.model

sealed class MarkdownBlock {
    data class Paragraph(val text: String) : MarkdownBlock()
    data class Heading(val level: Int, val text: String) : MarkdownBlock()
    data class Bullet(val text: String) : MarkdownBlock()
    data class Numbered(val index: String, val text: String) : MarkdownBlock()
    data class Quote(val text: String) : MarkdownBlock()
    data class Divider(val raw: String = "---") : MarkdownBlock()
    data class CodeBlock(val code: String) : MarkdownBlock()
    data class Table(val rows: List<String>) : MarkdownBlock()
    data class AsciiBlock(val content: String) : MarkdownBlock()
}