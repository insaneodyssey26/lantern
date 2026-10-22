package com.masum.lantern

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.masum.lantern.domain.repository.SettingsRepository
import com.masum.lantern.navigation.LanternNavHost
import com.masum.lantern.ui.theme.LanternTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initialThemeMode = settingsRepository.getThemeModeSync()
        val initialColorTheme = settingsRepository.getColorThemeSync()
        val initialPureBlack = settingsRepository.isPureBlackSync()
        val initialUseSystemFont = settingsRepository.isUseSystemFontSync()

        setContent {
            val themeMode by settingsRepository.getThemeMode().collectAsStateWithLifecycle(initialValue = initialThemeMode)
            val colorTheme by settingsRepository.getColorTheme().collectAsStateWithLifecycle(initialValue = initialColorTheme)
            val pureBlack by settingsRepository.isPureBlack().collectAsStateWithLifecycle(initialValue = initialPureBlack)
            val useSystemFont by settingsRepository.isUseSystemFont().collectAsStateWithLifecycle(initialValue = initialUseSystemFont)

            val darkTheme = when (themeMode) {
                "dark"  -> true
                "light" -> false
                else    -> isSystemInDarkTheme()
            }

            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { darkTheme }
                )
                onDispose {}
            }

            LanternTheme(
                darkTheme = darkTheme,
                colorTheme = colorTheme,
                pureBlack = pureBlack,
                useSystemFont = useSystemFont
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LanternNavHost()
                }
            }
        }
    }
}
