package com.syme.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap

/* ───────────────────────────── */
/* 🧩 Modèle de segment          */
/* ───────────────────────────── */
data class GradientSegment(
    val start: Float,
    val end: Float,
    val startColor: Color,
    val endColor: Color,
    val cap: StrokeCap
)
