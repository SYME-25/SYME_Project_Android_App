package com.syme.ui.snapshot

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object MessageColors {

    val SuccessGreen = Color(0xFF02FA0E)

    @Composable
    fun surfaceColor() = MaterialTheme.colorScheme.surface

    @Composable
    fun successIcon() = MaterialTheme.colorScheme.primary

    @Composable
    fun errorIcon() = MaterialTheme.colorScheme.error

    @Composable
    fun infoIcon() = MaterialTheme.colorScheme.secondary
}
