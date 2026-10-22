package com.masum.lantern

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
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
        enableEdgeToEdge()
        setContent {
            val themeMode by settingsRepository.getThemeMode().collectAsStateWithLifecycle(initialValue = "system")
            val useSystemFont by settingsRepository.isUseSystemFont().collectAsStateWithLifecycle(initialValue = false)

            val darkTheme = when (themeMode) {
                "dark"  -> true
                "light" -> false
                else    -> isSystemInDarkTheme()
            }

            LanternTheme(
                darkTheme = darkTheme,
                useSystemFont = useSystemFont
            ) {
                LanternNavHost()
            }
        }
    }
}
