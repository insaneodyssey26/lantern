package com.masum.lantern.feature.whitelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masum.lantern.domain.repository.AppInfoRepository
import com.masum.lantern.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppWhitelistViewModel @Inject constructor(
    private val appInfoRepository: AppInfoRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val installedApps = MutableStateFlow<List<com.masum.lantern.domain.model.AppInfo>>(emptyList())
    private val isLoading = MutableStateFlow(true)

    init {
        viewModelScope.launch {
            val apps = appInfoRepository.getInstalledApps()
            installedApps.value = apps
            isLoading.value = false
        }
    }

    val uiState: StateFlow<AppWhitelistUiState> = combine(
        installedApps,
        settingsRepository.getBypassedApps(),
        isLoading
    ) { apps, bypassedSet, loading ->
        val items = apps.map { app ->
            WhitelistAppItem(
                appInfo = app,
                isBypassed = bypassedSet.contains(app.packageName)
            )
        }
        AppWhitelistUiState(
            apps = items,
            bypassedPackages = bypassedSet,
            isLoading = loading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppWhitelistUiState()
    )

    fun toggleAppBypass(packageName: String, isBypassed: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAppBypassed(packageName, isBypassed)
        }
    }

    fun clearAllBypasses() {
        viewModelScope.launch {
            val current = uiState.value.bypassedPackages
            current.forEach { pkg ->
                settingsRepository.setAppBypassed(pkg, false)
            }
        }
    }
}
