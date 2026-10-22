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
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LanternDark = darkColorScheme(
    primary = LanternAmber,
    onPrimary = Color(0xFF4A2800),
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
    background = SurfaceDarkLowest,
    onBackground = TextPrimaryDark,
    surface = SurfaceDarkLow,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceDarkHigh,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainerLowest = SurfaceDarkLowest,
    surfaceContainerLow = SurfaceDarkLow,
    surfaceContainer = SurfaceDark,
    surfaceContainerHigh = SurfaceDarkHigh,
    surfaceContainerHighest = SurfaceDarkHighest,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)

private val LanternAmoled = darkColorScheme(
    primary = LanternAmber,
    onPrimary = Color(0xFF4A2800),
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
    background = PureBlackLowest,
    onBackground = TextPrimaryDark,
    surface = PureBlackLowest,
    onSurface = TextPrimaryDark,
    surfaceVariant = PureBlackHigh,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainerLowest = PureBlackLowest,
    surfaceContainerLow = PureBlackLow,
    surfaceContainer = PureBlack,
    surfaceContainerHigh = PureBlackHigh,
    surfaceContainerHighest = PureBlackHighest,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)

private val LanternLight = lightColorScheme(
    primary = LanternGold,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = LanternAmberBright,
    onPrimaryContainer = Color(0xFF2E1500),
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

private val EmberDark = LanternDark.copy(
    primary = EmberDarkPrimary,
    onPrimary = EmberDarkOnPrimary,
    primaryContainer = EmberDarkPrimaryContainer,
    onPrimaryContainer = EmberDarkOnPrimaryContainer
)

private val EmberAmoled = LanternAmoled.copy(
    primary = EmberDarkPrimary,
    onPrimary = EmberDarkOnPrimary,
    primaryContainer = EmberDarkPrimaryContainer,
    onPrimaryContainer = EmberDarkOnPrimaryContainer
)

private val EmberLight = LanternLight.copy(
    primary = EmberLightPrimary,
    onPrimary = EmberLightOnPrimary,
    primaryContainer = EmberLightPrimaryContainer,
    onPrimaryContainer = EmberLightOnPrimaryContainer
)

private val OceanDark = LanternDark.copy(
    primary = OceanDarkPrimary,
    onPrimary = OceanDarkOnPrimary,
    primaryContainer = OceanDarkPrimaryContainer,
    onPrimaryContainer = OceanDarkOnPrimaryContainer
)

private val OceanAmoled = LanternAmoled.copy(
    primary = OceanDarkPrimary,
    onPrimary = OceanDarkOnPrimary,
    primaryContainer = OceanDarkPrimaryContainer,
    onPrimaryContainer = OceanDarkOnPrimaryContainer
)

private val OceanLight = LanternLight.copy(
    primary = OceanLightPrimary,
    onPrimary = OceanLightOnPrimary,
    primaryContainer = OceanLightPrimaryContainer,
    onPrimaryContainer = OceanLightOnPrimaryContainer
)

private val EmeraldDark = LanternDark.copy(
    primary = EmeraldDarkPrimary,
    onPrimary = EmeraldDarkOnPrimary,
    primaryContainer = EmeraldDarkPrimaryContainer,
    onPrimaryContainer = EmeraldDarkOnPrimaryContainer
)

private val EmeraldAmoled = LanternAmoled.copy(
    primary = EmeraldDarkPrimary,
    onPrimary = EmeraldDarkOnPrimary,
    primaryContainer = EmeraldDarkPrimaryContainer,
    onPrimaryContainer = EmeraldDarkOnPrimaryContainer
)

private val EmeraldLight = LanternLight.copy(
    primary = EmeraldLightPrimary,
    onPrimary = EmeraldLightOnPrimary,
    primaryContainer = EmeraldLightPrimaryContainer,
    onPrimaryContainer = EmeraldLightOnPrimaryContainer
)

private val OrchidDark = LanternDark.copy(
    primary = OrchidDarkPrimary,
    onPrimary = OrchidDarkOnPrimary,
    primaryContainer = OrchidDarkPrimaryContainer,
    onPrimaryContainer = OrchidDarkOnPrimaryContainer
)

private val OrchidAmoled = LanternAmoled.copy(
    primary = OrchidDarkPrimary,
    onPrimary = OrchidDarkOnPrimary,
    primaryContainer = OrchidDarkPrimaryContainer,
    onPrimaryContainer = OrchidDarkOnPrimaryContainer
)

private val OrchidLight = LanternLight.copy(
    primary = OrchidLightPrimary,
    onPrimary = OrchidLightOnPrimary,
    primaryContainer = OrchidLightPrimaryContainer,
    onPrimaryContainer = OrchidLightOnPrimaryContainer
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LanternTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorTheme: String = "system",
    pureBlack: Boolean = false,
    useSystemFont: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val baseScheme = when (colorTheme) {
        "ember" -> when {
            darkTheme && pureBlack -> EmberAmoled
            darkTheme -> EmberDark
            else -> EmberLight
        }
        "ocean" -> when {
            darkTheme && pureBlack -> OceanAmoled
            darkTheme -> OceanDark
            else -> OceanLight
        }
        "emerald" -> when {
            darkTheme && pureBlack -> EmeraldAmoled
            darkTheme -> EmeraldDark
            else -> EmeraldLight
        }
        "orchid" -> when {
            darkTheme && pureBlack -> OrchidAmoled
            darkTheme -> OrchidDark
            else -> OrchidLight
        }
        "amber" -> when {
            darkTheme && pureBlack -> LanternAmoled
            darkTheme -> LanternDark
            else -> LanternLight
        }
        else -> {
            if (supportsDynamic) {
                if (darkTheme) {
                    val dynamicDark = dynamicDarkColorScheme(context)
                    if (pureBlack) {
                        dynamicDark.copy(
                            background = PureBlackLowest,
                            surface = PureBlackLowest,
                            surfaceContainerLowest = PureBlackLowest,
                            surfaceContainerLow = PureBlackLow,
                            surfaceContainer = PureBlack,
                            surfaceContainerHigh = PureBlackHigh,
                            surfaceContainerHighest = PureBlackHighest
                        )
                    } else {
                        dynamicDark
                    }
                } else {
                    dynamicLightColorScheme(context)
                }
            } else {
                when {
                    darkTheme && pureBlack -> LanternAmoled
                    darkTheme -> LanternDark
                    else -> LanternLight
                }
            }
        }
    }

    val typography = remember(useSystemFont) { getLanternTypography(useSystemFont) }

    MaterialExpressiveTheme(
        colorScheme = baseScheme,
        motionScheme = MotionScheme.expressive(),
        shapes = LanternShapes,
        typography = typography,
        content = content
    )
}
