package com.shishir.automatrix.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * AutoMatrix — monochrome design system.
 * Pure black & white palette inspired by iOS system colors.
 * One accent (white in dark mode, black in light mode) drives all highlights.
 */

// ---- Dark theme (true black) — mirrors iOS system colors on OLED ----
val DarkBackground = Color(0xFF000000)
val DarkSurface = Color(0xFF000000)
val DarkSurfaceVariant = Color(0xFF1C1C1E)   // secondarySystemGroupedBackground
val DarkGlassSurface = Color(0xFF1C1C1E)     // grouped card surface
val DarkGlassBorder = Color(0x24FFFFFF)      // separator (~14% white)
val DarkTextPrimary = Color(0xFFF2F2F7)      // label
val DarkTextSecondary = Color(0xFF9A9AA0)    // secondaryLabel
val DarkTextMuted = Color(0xFF636366)        // tertiaryLabel

// ---- Light theme — iOS grouped-list lightness ----
val LightBackground = Color(0xFFF2F2F7)      // systemGroupedBackground
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFFFFFFF)
val LightGlassSurface = Color(0xFFFFFFFF)
val LightGlassBorder = Color(0x1F3C3C43)     // separator (~12% label)
val LightTextPrimary = Color(0xFF000000)
val LightTextSecondary = Color(0xFF6D6D72)   // secondaryLabel
val LightTextMuted = Color(0xFF8E8E93)       // tertiaryLabel

// ---- Semantic states (the only non-monochrome accents, used sparingly) ----
val StatusSuccess = Color(0xFF30D158)        // systemGreen
val StatusWarning = Color(0xFF98989D)        // systemGray — warnings stay neutral in B&W
val StatusError = Color(0xFFFF453A)          // systemRed
