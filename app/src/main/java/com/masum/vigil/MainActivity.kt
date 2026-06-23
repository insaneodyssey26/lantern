package com.masum.vigil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.masum.vigil.domain.repository.SettingsRepository
import com.masum.vigil.navigation.VigilNavHost
import com.masum.vigil.ui.theme.VigilTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode  by settingsRepository.getThemeMode().collectAsStateWithLifecycle(initialValue = "system")
            val colorTheme by settingsRepository.getColorTheme().collectAsStateWithLifecycle(initialValue = "system")

            val darkTheme = when (themeMode) {
                "dark"  -> true
                "light" -> false
                else    -> isSystemInDarkTheme()
            }

            VigilTheme(darkTheme = darkTheme, colorTheme = colorTheme) {
                VigilNavHost()
            }
        }
    }
}
