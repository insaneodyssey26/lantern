package com.masum.lantern.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.masum.lantern.core.ui.theme.LanternTheme as CoreLanternTheme

@Composable
fun LanternTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useSystemFont: Boolean = false,
    content: @Composable () -> Unit,
) {
    CoreLanternTheme(
        darkTheme = darkTheme,
        useSystemFont = useSystemFont,
        content = content,
    )
}
