package com.masum.lantern.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.masum.lantern.core.ui.theme.LanternTheme as CoreLanternTheme

@Composable
fun LanternTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    pureBlack: Boolean = false,
    useSystemFont: Boolean = false,
    content: @Composable () -> Unit,
) {
    CoreLanternTheme(
        darkTheme = darkTheme,
        pureBlack = pureBlack,
        useSystemFont = useSystemFont,
        content = content,
    )
}
