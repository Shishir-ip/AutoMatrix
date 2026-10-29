package com.shishir.automatrix.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Shared surface/ink tokens used across the whole UI. Provided once by
 * [AutoMatrixTheme]; screens read them via `LocalFlowGlassColors.current`.
 */
data class FlowGlassColors(
    val glassSurface: Color,
    val glassBorder: Color,
    val glassCardHighlight: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    /** The single monochrome accent: white on dark, black on light. */
    val accent: Color,
    // Legacy aliases kept so older call sites keep compiling.
    val accentCyan: Color = accent,
    val accentIndigo: Color = accent,
    val accentRose: Color = accent
)

val LocalFlowGlassColors = staticCompositionLocalOf {
    FlowGlassColors(
        glassSurface = DarkGlassSurface,
        glassBorder = DarkGlassBorder,
        glassCardHighlight = Color(0x14FFFFFF),
        textPrimary = DarkTextPrimary,
        textSecondary = DarkTextSecondary,
        textMuted = DarkTextMuted,
        accent = Color.White
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    primaryContainer = Color.White,
    onPrimaryContainer = Color.Black,
    secondary = Color(0xFF9A9AA0),
    onSecondary = Color.Black,
    tertiary = Color(0xFF9A9AA0),
    error = Color(0xFFFF453A),
    onError = Color.White,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkGlassBorder,
    outlineVariant = DarkGlassBorder,
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF1C1C1E),
    surfaceContainer = Color(0xFF1C1C1E),
    surfaceContainerHigh = Color(0xFF232325),
    surfaceContainerHighest = Color(0xFF2C2C2E)
)

private val LightColorScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    primaryContainer = Color.Black,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF6D6D72),
    onSecondary = Color.White,
    tertiary = Color(0xFF6D6D72),
    error = Color(0xFFFF3B30),
    onError = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightGlassBorder,
    outlineVariant = LightGlassBorder,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFF7F7FA),
    surfaceContainerHighest = Color(0xFFF2F2F7)
)

@Composable
fun AutoMatrixTheme(
    themeMode: String = "system", // "system", "dark", "light"
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    val glassColors = if (isDark) {
        FlowGlassColors(
            glassSurface = DarkGlassSurface,
            glassBorder = DarkGlassBorder,
            glassCardHighlight = Color(0x14FFFFFF),
            textPrimary = DarkTextPrimary,
            textSecondary = DarkTextSecondary,
            textMuted = DarkTextMuted,
            accent = Color.White
        )
    } else {
        FlowGlassColors(
            glassSurface = LightGlassSurface,
            glassBorder = LightGlassBorder,
            glassCardHighlight = Color(0x0A000000),
            textPrimary = LightTextPrimary,
            textSecondary = LightTextSecondary,
            textMuted = LightTextMuted,
            accent = Color.Black
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(LocalFlowGlassColors provides glassColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
