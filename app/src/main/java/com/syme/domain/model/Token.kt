package com.syme.domain.model

// ─── Inline tokeniser ─────────────────────────────────────────────────────────

sealed class Token {
    data class Plain(val t: String)  : Token()
    data class Bold(val t: String)   : Token()
    data class Italic(val t: String) : Token()
    data class Code(val t: String)   : Token()
    data class Strike(val t: String) : Token()
}
