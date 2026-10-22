package com.masum.lantern.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LanternDark = darkColorScheme(
    primary = LanternAmber,
    onPrimary = Color(0xFF261900),
    primaryContainer = LanternContainerDark,
    onPrimaryContainer = LanternAmberBright,
    secondary = Color(0xFFE2C08D),
    onSecondary = Color(0xFF3F2D04),
    secondaryContainer = Color(0xFF584319),
    onSecondaryContainer = Color(0xFFFFDF9E),
    tertiary = VerdictSafe,
    onTertiary = Color(0xFF003822),
    tertiaryContainer = VerdictSafeContainer,
    onTertiaryContainer = Color(0xFFA7F3D0),
    error = VerdictTracker,
    onError = Color(0xFF4C0519),
    errorContainer = VerdictTrackerContainer,
    onErrorContainer = Color(0xFFFFD9DF),
    background = BasaltLowest,
    onBackground = TextPrimaryDark,
    surface = BasaltLow,
    onSurface = TextPrimaryDark,
    surfaceVariant = BasaltHigh,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainerLowest = BasaltLowest,
    surfaceContainerLow = BasaltLow,
    surfaceContainer = BasaltBase,
    surfaceContainerHigh = BasaltHigh,
    surfaceContainerHighest = BasaltHighest,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)

private val LanternLight = lightColorScheme(
    primary = LanternGold,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFE082),
    onPrimaryContainer = Color(0xFF241A00),
    secondary = Color(0xFF6D5B39),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF7DFBB),
    onSecondaryContainer = Color(0xFF251A01),
    tertiary = Color(0xFF059669),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFA7F3D0),
    onTertiaryContainer = Color(0xFF002113),
    error = Color(0xFFE11D48),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFD9DF),
    onErrorContainer = Color(0xFF400010),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8FAFC),
    surfaceContainer = Color(0xFFF1F5F9),
    surfaceContainerHigh = Color(0xFFE2E8F0),
    surfaceContainerHighest = Color(0xFFCBD5E1),
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFFE2E8F0)
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LanternTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) LanternDark else LanternLight

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        shapes = LanternShapes,
        typography = LanternTypography,
        content = content
    )
}
