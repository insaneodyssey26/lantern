package com.masum.lantern.ui.theme

import androidx.compose.runtime.Composable
import com.masum.lantern.core.ui.theme.LanternTheme as CoreLanternTheme

@Composable
fun LanternTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    colorTheme: String = "system",
    content: @Composable () -> Unit,
) = CoreLanternTheme(
    darkTheme    = darkTheme,
    dynamicColor = dynamicColor,
    colorTheme   = colorTheme,
    content      = content,
)
