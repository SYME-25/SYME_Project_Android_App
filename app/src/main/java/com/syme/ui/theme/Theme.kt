package com.syme.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary            = Brand500,
    onPrimary          = Neutral0,
    primaryContainer   = Brand100,
    onPrimaryContainer = Brand800,

    secondary            = Accent500,
    onSecondary          = Neutral0,
    secondaryContainer   = Accent200,
    onSecondaryContainer = Brand900,

    tertiary            = SemanticWarning500,
    onTertiary          = Neutral0,
    tertiaryContainer   = SemanticWarning100,
    onTertiaryContainer = Color(0xFF78350F),

    background          = Neutral50,
    onBackground        = Neutral900,

    surface             = Neutral0,
    onSurface           = Neutral900,
    surfaceVariant      = Neutral100,
    onSurfaceVariant    = Neutral600,

    outline             = Neutral200,
    outlineVariant      = Neutral100,

    error               = SemanticError500,
    onError             = Neutral0,
    errorContainer      = SemanticError100,
    onErrorContainer    = Color(0xFF7F1D1D),
)

private val DarkColorScheme = darkColorScheme(
    primary            = Brand300,
    onPrimary          = Brand900,
    primaryContainer   = Brand700,
    onPrimaryContainer = Brand100,

    secondary            = Accent400,
    onSecondary          = Brand900,
    secondaryContainer   = Color(0xFF004D41),
    onSecondaryContainer = Accent200,

    tertiary            = SemanticWarning500,
    onTertiary          = Neutral950,
    tertiaryContainer   = Color(0xFF78350F),
    onTertiaryContainer = SemanticWarning100,

    background          = Neutral950,
    onBackground        = Neutral100,

    surface             = Neutral900,
    onSurface           = Neutral100,
    surfaceVariant      = Neutral800,
    onSurfaceVariant    = Neutral400,

    outline             = Neutral700,
    outlineVariant      = Neutral800,

    error               = Color(0xFFF87171),
    onError             = Color(0xFF7F1D1D),
    errorContainer      = Color(0xFF991B1B),
    onErrorContainer    = SemanticError100,
)

@Composable
fun SYMETheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}