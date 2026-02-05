package com.syme.ui.snapshot

import androidx.compose.ui.graphics.vector.ImageVector

data class Message(
    val text: String,
    val icon: ImageVector? = null,
    val isError: Boolean = false,
    val isSystemMessage: Boolean = false
)