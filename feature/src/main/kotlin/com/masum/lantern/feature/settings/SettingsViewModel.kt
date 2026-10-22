package com.masum.lantern.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masum.lantern.domain.repository.BlocklistRepository
import com.masum.lantern.domain.repository.DnsLogRepository
import com.masum.lantern.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val dnsLogRepository: DnsLogRepository,
    private val blocklistRepository: BlocklistRepository,
) : ViewModel() {

    private val isClearingLogs = MutableStateFlow(false)
    private val isUpdatingBlocklist = MutableStateFlow(false)
    private val blocklistUpdateMessage = MutableStateFlow<String?>(null)
    private val blocklistRefreshTrigger = MutableStateFlow(0)

    private val settingsFlow = combine(
        settingsRepository.getThemeMode(),
        settingsRepository.getColorTheme(),
        settingsRepository.isPureBlack(),
        settingsRepository.isUseSystemFont(),
        settingsRepository.getUpstreamDns(),
        settingsRepository.getMaxAgeDays(),
        settingsRepository.getMaxRows()
    ) { args: Array<Any> ->
        SettingsTuple(
            themeMode = args[0] as String,
            colorTheme = args[1] as String,
            pureBlack = args[2] as Boolean,
            useSystemFont = args[3] as Boolean,
            upstreamDns = args[4] as String,
            maxAgeDays = args[5] as Int,
            maxRows = args[6] as Int
        )
    }

    private val totalLogCountFlow = flow {
        emit(0L)
        delay(400)
        dnsLogRepository.observeTotalCount().collect { count ->
            emit(count.toLong())
        }
    }

    private val blocklistStateFlow = combine(
        isUpdatingBlocklist,
        blocklistUpdateMessage,
        blocklistRefreshTrigger
    ) { updating, message, _ ->
        Triple(updating, message, blocklistRepository.getLastUpdatedTimestamp())
    }

    private val domainAndBypassedFlow = combine(
        settingsRepository.getBypassedApps(),
        settingsRepository.getUserAllowedDomains(),
        settingsRepository.getUserBlockedDomains()
    ) { bypassed, allowed, blocked ->
        Pair(bypassed.size, allowed.size + blocked.size)
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsFlow,
        totalLogCountFlow,
        isClearingLogs,
        blocklistStateFlow,
        domainAndBypassedFlow
    ) { settings, logCount, clearing, blocklistState, domainAndBypassed ->
        SettingsUiState(
            themeMode = settings.themeMode,
            colorTheme = settings.colorTheme,
            pureBlack = settings.pureBlack,
            useSystemFont = settings.useSystemFont,
            upstreamDns = settings.upstreamDns,
            maxAgeDays = settings.maxAgeDays,
            maxRows = settings.maxRows,
            totalLogCount = logCount,
            blocklistCount = blocklistRepository.domainCount(),
            blocklistLastUpdated = blocklistState.third,
            isClearingLogs = clearing,
            isUpdatingBlocklist = blocklistState.first,
            blocklistUpdateMessage = blocklistState.second,
            bypassedAppsCount = domainAndBypassed.first,
            domainRulesCount = domainAndBypassed.second
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun setColorTheme(theme: String) {
        viewModelScope.launch {
            settingsRepository.setColorTheme(theme)
        }
    }

    fun setUpstreamDns(dns: String) {
        viewModelScope.launch {
            settingsRepository.setUpstreamDns(dns)
        }
    }

    fun setMaxAgeDays(days: Int) {
        viewModelScope.launch {
            settingsRepository.setMaxAgeDays(days)
        }
    }

    fun setMaxRows(rows: Int) {
        viewModelScope.launch {
            settingsRepository.setMaxRows(rows)
        }
    }

    fun setPureBlack(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setPureBlack(enabled)
        }
    }

    fun setUseSystemFont(useSystem: Boolean) {
        viewModelScope.launch {
            settingsRepository.setUseSystemFont(useSystem)
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            isClearingLogs.value = true
            dnsLogRepository.clearHistory()
            isClearingLogs.value = false
        }
    }

    fun updateBlocklist() {
        viewModelScope.launch {
            if (isUpdatingBlocklist.value) return@launch
            isUpdatingBlocklist.value = true
            blocklistUpdateMessage.value = null
            val result = blocklistRepository.updateBlocklist()
            isUpdatingBlocklist.value = false
            result.fold(
                onSuccess = { updated ->
                    blocklistRefreshTrigger.value += 1
                    blocklistUpdateMessage.value = if (updated) {
                        "Successfully updated threat database to latest rules"
                    } else {
                        "Database is already up to date"
                    }
                },
                onFailure = {
                    blocklistUpdateMessage.value = "Failed to update: Check internet connection"
                }
            )
        }
    }

    fun clearBlocklistMessage() {
        blocklistUpdateMessage.value = null
    }
}

data class SettingsTuple(
    val themeMode: String,
    val colorTheme: String,
    val pureBlack: Boolean,
    val useSystemFont: Boolean,
    val upstreamDns: String,
    val maxAgeDays: Int,
    val maxRows: Int,
)
