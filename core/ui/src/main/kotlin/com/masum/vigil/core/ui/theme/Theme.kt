package com.masum.vigil.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ─────────────────────────────────────────────────────────────────────────────
// VIGIL — Teal/cyan primary, deep purple secondary.
// Sharp, precise, trustworthy. Like a terminal.
// ─────────────────────────────────────────────────────────────────────────────
private val VigilDark = darkColorScheme(
    primary             = Color(0xFF00E5FF),
    onPrimary           = Color(0xFF00363D),
    primaryContainer    = Color(0xFF004F58),
    onPrimaryContainer  = Color(0xFFE0F7FA),
    secondary           = Color(0xFFB39DDB),
    onSecondary         = Color(0xFF311B92),
    secondaryContainer  = Color(0xFF1E1035),
    onSecondaryContainer= Color(0xFFEDE7F6),
    tertiary            = Color(0xFF10B981),
    onTertiary          = Color(0xFF064E3B),
    tertiaryContainer   = Color(0xFF022C22),
    onTertiaryContainer = Color(0xFFA7F3D0),
    error               = Color(0xFFEF4444),
    onError             = Color(0xFF7F1D1D),
    errorContainer      = Color(0xFF3B0712),
    onErrorContainer    = Color(0xFFFEE2E2),
    background          = Color(0xFF090D10),
    onBackground        = Color(0xFFE2E8F0),
    surface             = Color(0xFF0F1418),
    onSurface           = Color(0xFFF1F5F9),
    surfaceVariant      = Color(0xFF172026),
    onSurfaceVariant    = Color(0xFF94A3B8),
    outline             = Color(0xFF475569),
    outlineVariant      = Color(0xFF334155),
)
private val VigilLight = lightColorScheme(
    primary             = Color(0xFF006874),
    onPrimary           = Color(0xFFFFFFFF),
    primaryContainer    = Color(0xFF97F0FF),
    onPrimaryContainer  = Color(0xFF001F24),
    secondary           = Color(0xFF5E35B1),
    onSecondary         = Color(0xFFFFFFFF),
    secondaryContainer  = Color(0xFFEDE7F6),
    onSecondaryContainer= Color(0xFF311B92),
    tertiary            = Color(0xFF00796B),
    onTertiary          = Color(0xFFFFFFFF),
    tertiaryContainer   = Color(0xFFE0F2F1),
    onTertiaryContainer = Color(0xFF004D40),
    error               = Color(0xFFBA1A1A),
    onError             = Color(0xFFFFFFFF),
    errorContainer      = Color(0xFFFFDAD6),
    onErrorContainer    = Color(0xFF410002),
    background          = Color(0xFFF8FAFC),
    onBackground        = Color(0xFF0F172A),
    surface             = Color(0xFFFFFFFF),
    onSurface           = Color(0xFF0F172A),
    surfaceVariant      = Color(0xFFE2E8F0),
    onSurfaceVariant    = Color(0xFF475569),
    outline             = Color(0xFF94A3B8),
    outlineVariant      = Color(0xFFCBD5E1),
)

// ─────────────────────────────────────────────────────────────────────────────
// STORM — Indigo/blue. Cold, precise, methodical.
// ─────────────────────────────────────────────────────────────────────────────
private val StormDark = darkColorScheme(
    primary             = Color(0xFF818CF8),
    onPrimary           = Color(0xFF1E1B4B),
    primaryContainer    = Color(0xFF312E81),
    onPrimaryContainer  = Color(0xFFE0E7FF),
    secondary           = Color(0xFF67E8F9),
    onSecondary         = Color(0xFF083344),
    secondaryContainer  = Color(0xFF0E4F60),
    onSecondaryContainer= Color(0xFFCFFAFE),
    tertiary            = Color(0xFF34D399),
    onTertiary          = Color(0xFF022C22),
    tertiaryContainer   = Color(0xFF064E3B),
    onTertiaryContainer = Color(0xFFA7F3D0),
    error               = Color(0xFFF87171),
    onError             = Color(0xFF450A0A),
    errorContainer      = Color(0xFF7F1D1D),
    onErrorContainer    = Color(0xFFFEE2E2),
    background          = Color(0xFF0A0C14),
    onBackground        = Color(0xFFE2E8F0),
    surface             = Color(0xFF0F1120),
    onSurface           = Color(0xFFF1F5F9),
    surfaceVariant      = Color(0xFF1A1F3A),
    onSurfaceVariant    = Color(0xFF94A3B8),
    outline             = Color(0xFF3730A3),
    outlineVariant      = Color(0xFF1E1B4B),
)
private val StormLight = lightColorScheme(
    primary             = Color(0xFF4338CA),
    onPrimary           = Color(0xFFFFFFFF),
    primaryContainer    = Color(0xFFE0E7FF),
    onPrimaryContainer  = Color(0xFF1E1B4B),
    secondary           = Color(0xFF0891B2),
    onSecondary         = Color(0xFFFFFFFF),
    secondaryContainer  = Color(0xFFCFFAFE),
    onSecondaryContainer= Color(0xFF083344),
    tertiary            = Color(0xFF059669),
    onTertiary          = Color(0xFFFFFFFF),
    tertiaryContainer   = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF064E3B),
    error               = Color(0xFFDC2626),
    onError             = Color(0xFFFFFFFF),
    errorContainer      = Color(0xFFFEE2E2),
    onErrorContainer    = Color(0xFF7F1D1D),
    background          = Color(0xFFF5F7FF),
    onBackground        = Color(0xFF0F172A),
    surface             = Color(0xFFFFFFFF),
    onSurface           = Color(0xFF1E1B4B),
    surfaceVariant      = Color(0xFFEEF2FF),
    onSurfaceVariant    = Color(0xFF4338CA),
    outline             = Color(0xFFA5B4FC),
    outlineVariant      = Color(0xFFE0E7FF),
)

// ─────────────────────────────────────────────────────────────────────────────
// EMBER — Amber/orange. Warm, alert-forward, vigilant.
// ─────────────────────────────────────────────────────────────────────────────
private val EmberDark = darkColorScheme(
    primary             = Color(0xFFFBBF24),
    onPrimary           = Color(0xFF1C0F00),
    primaryContainer    = Color(0xFF78350F),
    onPrimaryContainer  = Color(0xFFFEF3C7),
    secondary           = Color(0xFFF97316),
    onSecondary         = Color(0xFF1C0700),
    secondaryContainer  = Color(0xFF7C2D12),
    onSecondaryContainer= Color(0xFFFFEDD5),
    tertiary            = Color(0xFF4ADE80),
    onTertiary          = Color(0xFF052E16),
    tertiaryContainer   = Color(0xFF14532D),
    onTertiaryContainer = Color(0xFFBBF7D0),
    error               = Color(0xFFFC8181),
    onError             = Color(0xFF450A0A),
    errorContainer      = Color(0xFF7F1D1D),
    onErrorContainer    = Color(0xFFFEE2E2),
    background          = Color(0xFF0D0A00),
    onBackground        = Color(0xFFFEF9EE),
    surface             = Color(0xFF141009),
    onSurface           = Color(0xFFFEF9EE),
    surfaceVariant      = Color(0xFF231C0A),
    onSurfaceVariant    = Color(0xFFD4A855),
    outline             = Color(0xFF92400E),
    outlineVariant      = Color(0xFF451A03),
)
private val EmberLight = lightColorScheme(
    primary             = Color(0xFFD97706),
    onPrimary           = Color(0xFFFFFFFF),
    primaryContainer    = Color(0xFFFEF3C7),
    onPrimaryContainer  = Color(0xFF78350F),
    secondary           = Color(0xFFEA580C),
    onSecondary         = Color(0xFFFFFFFF),
    secondaryContainer  = Color(0xFFFFEDD5),
    onSecondaryContainer= Color(0xFF7C2D12),
    tertiary            = Color(0xFF16A34A),
    onTertiary          = Color(0xFFFFFFFF),
    tertiaryContainer   = Color(0xFFDCFCE7),
    onTertiaryContainer = Color(0xFF14532D),
    error               = Color(0xFFDC2626),
    onError             = Color(0xFFFFFFFF),
    errorContainer      = Color(0xFFFEE2E2),
    onErrorContainer    = Color(0xFF7F1D1D),
    background          = Color(0xFFFFFBF0),
    onBackground        = Color(0xFF1C0F00),
    surface             = Color(0xFFFFFFFF),
    onSurface           = Color(0xFF1C0F00),
    surfaceVariant      = Color(0xFFFEF3C7),
    onSurfaceVariant    = Color(0xFF92400E),
    outline             = Color(0xFFFBBF24),
    outlineVariant      = Color(0xFFFDE68A),
)

// ─────────────────────────────────────────────────────────────────────────────
// ORCHID — Deep rose/violet. Elegant, distinct from every other security app.
// ─────────────────────────────────────────────────────────────────────────────
private val OrchidDark = darkColorScheme(
    primary             = Color(0xFFE879F9),
    onPrimary           = Color(0xFF2D0060),
    primaryContainer    = Color(0xFF5B21B6),
    onPrimaryContainer  = Color(0xFFF5D0FE),
    secondary           = Color(0xFF818CF8),
    onSecondary         = Color(0xFF1E1B4B),
    secondaryContainer  = Color(0xFF312E81),
    onSecondaryContainer= Color(0xFFE0E7FF),
    tertiary            = Color(0xFF2DD4BF),
    onTertiary          = Color(0xFF042F2E),
    tertiaryContainer   = Color(0xFF134E4A),
    onTertiaryContainer = Color(0xFF99F6E4),
    error               = Color(0xFFF87171),
    onError             = Color(0xFF450A0A),
    errorContainer      = Color(0xFF7F1D1D),
    onErrorContainer    = Color(0xFFFEE2E2),
    background          = Color(0xFF0D000F),
    onBackground        = Color(0xFFF5E6FF),
    surface             = Color(0xFF120018),
    onSurface           = Color(0xFFF5E6FF),
    surfaceVariant      = Color(0xFF1F0030),
    onSurfaceVariant    = Color(0xFFC084FC),
    outline             = Color(0xFF7E22CE),
    outlineVariant      = Color(0xFF3B0764),
)
private val OrchidLight = lightColorScheme(
    primary             = Color(0xFF9333EA),
    onPrimary           = Color(0xFFFFFFFF),
    primaryContainer    = Color(0xFFF5D0FE),
    onPrimaryContainer  = Color(0xFF4C1D95),
    secondary           = Color(0xFF6366F1),
    onSecondary         = Color(0xFFFFFFFF),
    secondaryContainer  = Color(0xFFEEF2FF),
    onSecondaryContainer= Color(0xFF312E81),
    tertiary            = Color(0xFF0D9488),
    onTertiary          = Color(0xFFFFFFFF),
    tertiaryContainer   = Color(0xFFCCFBF1),
    onTertiaryContainer = Color(0xFF134E4A),
    error               = Color(0xFFDC2626),
    onError             = Color(0xFFFFFFFF),
    errorContainer      = Color(0xFFFEE2E2),
    onErrorContainer    = Color(0xFF7F1D1D),
    background          = Color(0xFFFDF4FF),
    onBackground        = Color(0xFF1C0030),
    surface             = Color(0xFFFFFFFF),
    onSurface           = Color(0xFF1C0030),
    surfaceVariant      = Color(0xFFF3E8FF),
    onSurfaceVariant    = Color(0xFF7E22CE),
    outline             = Color(0xFFD8B4FE),
    outlineVariant      = Color(0xFFF5D0FE),
)

// ─────────────────────────────────────────────────────────────────────────────
// CATPPUCCIN — Mocha (dark) / Latte (light)
// Soothing pastel palette. Mauve primary, beloved by every dev who's
// stared at Neovim for too long. https://catppuccin.com
// ─────────────────────────────────────────────────────────────────────────────
private val CatppuccinDark = darkColorScheme(  // Mocha
    primary             = Color(0xFFCBA6F7),   // Mauve
    onPrimary           = Color(0xFF1E1E2E),   // Base
    primaryContainer    = Color(0xFF45475A),   // Surface1
    onPrimaryContainer  = Color(0xFFCDD6F4),   // Text
    secondary           = Color(0xFF89B4FA),   // Blue
    onSecondary         = Color(0xFF1E1E2E),
    secondaryContainer  = Color(0xFF313244),   // Surface0
    onSecondaryContainer= Color(0xFFBAC2DE),   // Subtext1
    tertiary            = Color(0xFF94E2D5),   // Teal
    onTertiary          = Color(0xFF1E1E2E),
    tertiaryContainer   = Color(0xFF313244),
    onTertiaryContainer = Color(0xFFA6E3A1),   // Green
    error               = Color(0xFFF38BA8),   // Red
    onError             = Color(0xFF1E1E2E),
    errorContainer      = Color(0xFF45475A),
    onErrorContainer    = Color(0xFFF38BA8),
    background          = Color(0xFF1E1E2E),   // Base
    onBackground        = Color(0xFFCDD6F4),   // Text
    surface             = Color(0xFF181825),   // Mantle
    onSurface           = Color(0xFFCDD6F4),
    surfaceVariant      = Color(0xFF313244),   // Surface0
    onSurfaceVariant    = Color(0xFFBAC2DE),   // Subtext1
    outline             = Color(0xFF6C7086),   // Overlay0
    outlineVariant      = Color(0xFF45475A),   // Surface1
)
private val CatppuccinLight = lightColorScheme(  // Latte
    primary             = Color(0xFF8839EF),   // Mauve
    onPrimary           = Color(0xFFFFFFFF),
    primaryContainer    = Color(0xFFE6C9FF),
    onPrimaryContainer  = Color(0xFF5C0087),
    secondary           = Color(0xFF1E66F5),   // Blue
    onSecondary         = Color(0xFFFFFFFF),
    secondaryContainer  = Color(0xFFDDE3FF),
    onSecondaryContainer= Color(0xFF001452),
    tertiary            = Color(0xFF179299),   // Teal
    onTertiary          = Color(0xFFFFFFFF),
    tertiaryContainer   = Color(0xFFBCEBED),
    onTertiaryContainer = Color(0xFF002021),
    error               = Color(0xFFD20F39),   // Red
    onError             = Color(0xFFFFFFFF),
    errorContainer      = Color(0xFFFFDAD6),
    onErrorContainer    = Color(0xFF410002),
    background          = Color(0xFFEFF1F5),   // Base
    onBackground        = Color(0xFF4C4F69),   // Text
    surface             = Color(0xFFE6E9EF),   // Mantle
    onSurface           = Color(0xFF4C4F69),
    surfaceVariant      = Color(0xFFCCD0DA),   // Surface0
    onSurfaceVariant    = Color(0xFF5C5F77),   // Subtext0
    outline             = Color(0xFF9CA0B0),   // Overlay1
    outlineVariant      = Color(0xFFBCC0CC),   // Surface2
)

// ─────────────────────────────────────────────────────────────────────────────
// GRUVBOX — Dark (dark) / Light (light)
// Retro groove. Warm earthy tones, aged paper feeling.
// Created by morhetz. The OG of comfort themes.
// ─────────────────────────────────────────────────────────────────────────────
private val GruvboxDark = darkColorScheme(
    primary             = Color(0xFFFABD2F),   // Yellow
    onPrimary           = Color(0xFF282828),   // bg0
    primaryContainer    = Color(0xFF3C3836),   // bg1
    onPrimaryContainer  = Color(0xFFEBDBB2),   // fg
    secondary           = Color(0xFFFE8019),   // Orange
    onSecondary         = Color(0xFF282828),
    secondaryContainer  = Color(0xFF3C3836),
    onSecondaryContainer= Color(0xFFEBDBB2),
    tertiary            = Color(0xFFB8BB26),   // Green
    onTertiary          = Color(0xFF282828),
    tertiaryContainer   = Color(0xFF3C3836),
    onTertiaryContainer = Color(0xFFB8BB26),
    error               = Color(0xFFFB4934),   // Red
    onError             = Color(0xFF282828),
    errorContainer      = Color(0xFF3C3836),
    onErrorContainer    = Color(0xFFFB4934),
    background          = Color(0xFF1D2021),   // bg hard
    onBackground        = Color(0xFFEBDBB2),   // fg
    surface             = Color(0xFF282828),   // bg0
    onSurface           = Color(0xFFEBDBB2),
    surfaceVariant      = Color(0xFF3C3836),   // bg1
    onSurfaceVariant    = Color(0xFFA89984),   // fg4
    outline             = Color(0xFF665C54),   // bg3
    outlineVariant      = Color(0xFF504945),   // bg2
)
private val GruvboxLight = lightColorScheme(
    primary             = Color(0xFFB57614),   // Yellow darkened for contrast
    onPrimary           = Color(0xFFFFFFFF),
    primaryContainer    = Color(0xFFF2E5BC),
    onPrimaryContainer  = Color(0xFF3C3836),
    secondary           = Color(0xFFAF3A03),   // Orange
    onSecondary         = Color(0xFFFFFFFF),
    secondaryContainer  = Color(0xFFF2C5A0),
    onSecondaryContainer= Color(0xFF3C3836),
    tertiary            = Color(0xFF79740E),   // Green
    onTertiary          = Color(0xFFFFFFFF),
    tertiaryContainer   = Color(0xFFDFE5A4),
    onTertiaryContainer = Color(0xFF3C3836),
    error               = Color(0xFF9D0006),   // Red
    onError             = Color(0xFFFFFFFF),
    errorContainer      = Color(0xFFF4B8B0),
    onErrorContainer    = Color(0xFF3C3836),
    background          = Color(0xFFF9F5D7),   // bg0 hard
    onBackground        = Color(0xFF3C3836),   // fg1
    surface             = Color(0xFFFBF1C7),   // bg0
    onSurface           = Color(0xFF3C3836),
    surfaceVariant      = Color(0xFFEBDBB2),   // bg2
    onSurfaceVariant    = Color(0xFF665C54),   // fg3
    outline             = Color(0xFFBDAE93),   // bg4
    outlineVariant      = Color(0xFFD5C4A1),   // bg3
)

// ─────────────────────────────────────────────────────────────────────────────
// NORD — Arctic, north-bluish color palette.
// By Arctic Ice Studio. Clean, calm, everything is frozen.
// ─────────────────────────────────────────────────────────────────────────────
private val NordDark = darkColorScheme(
    primary             = Color(0xFF88C0D0),   // Nord8 — Frost
    onPrimary           = Color(0xFF2E3440),   // Nord0 — Polar Night
    primaryContainer    = Color(0xFF3B4252),   // Nord1
    onPrimaryContainer  = Color(0xFFECEFF4),   // Nord6
    secondary           = Color(0xFF81A1C1),   // Nord9 — Frost blue
    onSecondary         = Color(0xFF2E3440),
    secondaryContainer  = Color(0xFF434C5E),   // Nord2
    onSecondaryContainer= Color(0xFFD8DEE9),   // Nord4
    tertiary            = Color(0xFFA3BE8C),   // Nord14 — Aurora green
    onTertiary          = Color(0xFF2E3440),
    tertiaryContainer   = Color(0xFF3B4252),
    onTertiaryContainer = Color(0xFFA3BE8C),
    error               = Color(0xFFBF616A),   // Nord11 — Aurora red
    onError             = Color(0xFF2E3440),
    errorContainer      = Color(0xFF434C5E),
    onErrorContainer    = Color(0xFFBF616A),
    background          = Color(0xFF242933),   // between Nord0 and Nord1
    onBackground        = Color(0xFFECEFF4),   // Nord6
    surface             = Color(0xFF2E3440),   // Nord0
    onSurface           = Color(0xFFECEFF4),
    surfaceVariant      = Color(0xFF3B4252),   // Nord1
    onSurfaceVariant    = Color(0xFFD8DEE9),   // Nord4
    outline             = Color(0xFF4C566A),   // Nord3
    outlineVariant      = Color(0xFF434C5E),   // Nord2
)
private val NordLight = lightColorScheme(
    primary             = Color(0xFF5E81AC),   // Nord10 — Frost deep
    onPrimary           = Color(0xFFFFFFFF),
    primaryContainer    = Color(0xFFD8E8F4),
    onPrimaryContainer  = Color(0xFF2E3440),
    secondary           = Color(0xFF81A1C1),   // Nord9
    onSecondary         = Color(0xFFFFFFFF),
    secondaryContainer  = Color(0xFFE5EEF6),
    onSecondaryContainer= Color(0xFF2E3440),
    tertiary            = Color(0xFFA3BE8C),   // Nord14
    onTertiary          = Color(0xFF2E3440),
    tertiaryContainer   = Color(0xFFE8F0DF),
    onTertiaryContainer = Color(0xFF2E3440),
    error               = Color(0xFFBF616A),   // Nord11
    onError             = Color(0xFFFFFFFF),
    errorContainer      = Color(0xFFF5DBDE),
    onErrorContainer    = Color(0xFF2E3440),
    background          = Color(0xFFECEFF4),   // Nord6
    onBackground        = Color(0xFF2E3440),   // Nord0
    surface             = Color(0xFFFFFFFF),
    onSurface           = Color(0xFF2E3440),
    surfaceVariant      = Color(0xFFE5E9F0),   // Nord5
    onSurfaceVariant    = Color(0xFF4C566A),   // Nord3
    outline             = Color(0xFF88C0D0),   // Nord8
    outlineVariant      = Color(0xFFD8DEE9),   // Nord4
)

// ─────────────────────────────────────────────────────────────────────────────
// DRACULA — The vampire theme. Dark, dramatic, pink & purple.
// Created by Zeno Rocha. Lives in every editor since 2013.
// ─────────────────────────────────────────────────────────────────────────────
private val DraculaDark = darkColorScheme(
    primary             = Color(0xFFBD93F9),   // Purple
    onPrimary           = Color(0xFF282A36),   // Background
    primaryContainer    = Color(0xFF44475A),   // Current Line
    onPrimaryContainer  = Color(0xFFF8F8F2),   // Foreground
    secondary           = Color(0xFFFF79C6),   // Pink
    onSecondary         = Color(0xFF282A36),
    secondaryContainer  = Color(0xFF44475A),
    onSecondaryContainer= Color(0xFFF8F8F2),
    tertiary            = Color(0xFF50FA7B),   // Green
    onTertiary          = Color(0xFF282A36),
    tertiaryContainer   = Color(0xFF44475A),
    onTertiaryContainer = Color(0xFF50FA7B),
    error               = Color(0xFFFF5555),   // Red
    onError             = Color(0xFF282A36),
    errorContainer      = Color(0xFF44475A),
    onErrorContainer    = Color(0xFFFF5555),
    background          = Color(0xFF21222C),   // Slightly darker than bg
    onBackground        = Color(0xFFF8F8F2),   // Foreground
    surface             = Color(0xFF282A36),   // Background
    onSurface           = Color(0xFFF8F8F2),
    surfaceVariant      = Color(0xFF44475A),   // Current Line
    onSurfaceVariant    = Color(0xFF6272A4),   // Comment
    outline             = Color(0xFF6272A4),   // Comment
    outlineVariant      = Color(0xFF44475A),   // Current Line
)
// Dracula doesn't have an official light variant — use a desaturated warm
// interpretation that feels like Dracula in daylight.
private val DraculaLight = lightColorScheme(
    primary             = Color(0xFF6D4BB4),   // Purple darkened for light bg
    onPrimary           = Color(0xFFFFFFFF),
    primaryContainer    = Color(0xFFEDE4FF),
    onPrimaryContainer  = Color(0xFF280072),
    secondary           = Color(0xFFCC3D87),   // Pink darkened
    onSecondary         = Color(0xFFFFFFFF),
    secondaryContainer  = Color(0xFFFFD8ED),
    onSecondaryContainer= Color(0xFF540034),
    tertiary            = Color(0xFF1B7B36),   // Green darkened
    onTertiary          = Color(0xFFFFFFFF),
    tertiaryContainer   = Color(0xFFB7F7C3),
    onTertiaryContainer = Color(0xFF00210E),
    error               = Color(0xFFCC2222),   // Red darkened
    onError             = Color(0xFFFFFFFF),
    errorContainer      = Color(0xFFFFDAD6),
    onErrorContainer    = Color(0xFF410002),
    background          = Color(0xFFF7F6FF),   // Hint of the Dracula purple
    onBackground        = Color(0xFF282A36),   // Same as dark bg — readable
    surface             = Color(0xFFFFFFFF),
    onSurface           = Color(0xFF282A36),
    surfaceVariant      = Color(0xFFEEECFF),
    onSurfaceVariant    = Color(0xFF6272A4),   // Comment color works both ways
    outline             = Color(0xFFBDB8D8),
    outlineVariant      = Color(0xFFDDDAF5),
)

// ─────────────────────────────────────────────────────────────────────────────
// Public API
// ─────────────────────────────────────────────────────────────────────────────

fun colorSchemeFor(key: String, dark: Boolean) = when (key) {
    "storm"      -> if (dark) StormDark      else StormLight
    "ember"      -> if (dark) EmberDark      else EmberLight
    "orchid"     -> if (dark) OrchidDark     else OrchidLight
    "catppuccin" -> if (dark) CatppuccinDark else CatppuccinLight
    "gruvbox"    -> if (dark) GruvboxDark    else GruvboxLight
    "nord"       -> if (dark) NordDark       else NordLight
    "dracula"    -> if (dark) DraculaDark    else DraculaLight
    else         -> if (dark) VigilDark      else VigilLight   // "vigil" + fallback
}

@Composable
fun VigilTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    colorTheme: String = "system",
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        colorTheme == "system" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> colorSchemeFor(colorTheme, darkTheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = VigilTypography,
        shapes      = VigilShapes,
        content     = content,
    )
}
