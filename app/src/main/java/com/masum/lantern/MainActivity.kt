package com.masum.lantern

import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.masum.lantern.domain.repository.SettingsRepository
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.masum.lantern.navigation.LanternNavHost
import com.masum.lantern.ui.theme.LanternTheme
import com.masum.lantern.updates.PlayUpdateManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        val splashStartTime = SystemClock.uptimeMillis()
        splashScreen.setKeepOnScreenCondition {
            SystemClock.uptimeMillis() - splashStartTime < 1700L
        }
        super.onCreate(savedInstanceState)
        val initialThemeMode = settingsRepository.getThemeModeSync()
        val initialColorTheme = settingsRepository.getColorThemeSync()
        val initialPureBlack = settingsRepository.isPureBlackSync()
        val initialUseSystemFont = settingsRepository.isUseSystemFontSync()
        val isOnboardingCompleted = settingsRepository.isOnboardingCompletedSync()

        var isPlayUpdateReady by mutableStateOf(false)
        PlayUpdateManager.checkForPlayUpdates(this) {
            isPlayUpdateReady = true
        }

        setContent {
            val themeMode by settingsRepository.getThemeMode().collectAsStateWithLifecycle(initialValue = initialThemeMode)
            val colorTheme by settingsRepository.getColorTheme().collectAsStateWithLifecycle(initialValue = initialColorTheme)
            val pureBlack by settingsRepository.isPureBlack().collectAsStateWithLifecycle(initialValue = initialPureBlack)
            val useSystemFont by settingsRepository.isUseSystemFont().collectAsStateWithLifecycle(initialValue = initialUseSystemFont)
            val onboardingCompleted by settingsRepository.isOnboardingCompleted().collectAsStateWithLifecycle(initialValue = isOnboardingCompleted)

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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = false
                }
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
                    val startDestination = if (onboardingCompleted) {
                        com.masum.lantern.navigation.LanternDestination.Home.route
                    } else {
                        "onboarding"
                    }
                    Box(modifier = Modifier.fillMaxSize()) {
                        LanternNavHost(startDestination = startDestination)

                        if (isPlayUpdateReady) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Snackbar(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .navigationBarsPadding(),
                                    action = {
                                        TextButton(onClick = { PlayUpdateManager.completeUpdate(this@MainActivity) }) {
                                            Text("RESTART", color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                ) {
                                    Text("An update is ready to install.")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
