package com.masum.vigil.ui.theme

import androidx.compose.runtime.Composable
import com.masum.vigil.core.ui.theme.VigilTheme as CoreVigilTheme

@Composable
fun VigilTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    colorTheme: String = "system",
    content: @Composable () -> Unit,
) = CoreVigilTheme(
    darkTheme    = darkTheme,
    dynamicColor = dynamicColor,
    colorTheme   = colorTheme,
    content      = content,
)
