package com.syme.domain.model

import androidx.compose.ui.graphics.Color

// ─── Data class ───────────────────────────────────────────────────────────────

data class FieldColors(
    val container: Color,
    val border: Color,
    val icon: Color,
    val label: Color,
    val text: Color,
    val cursor: Color,
    val isDark: Boolean
)
