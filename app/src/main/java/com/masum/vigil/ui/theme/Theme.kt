package com.masum.vigil.ui.theme

import androidx.compose.runtime.Composable
import com.masum.vigil.core.ui.theme.VigilTheme as CoreVigilTheme

@Composable
fun VigilTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) = CoreVigilTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
