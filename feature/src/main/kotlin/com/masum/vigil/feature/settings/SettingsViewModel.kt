package com.masum.vigil.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masum.vigil.domain.repository.BlocklistRepository
import com.masum.vigil.domain.repository.DnsLogRepository
import com.masum.vigil.domain.repository.SettingsRepository
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

    private val settingsFlow = combine(
        settingsRepository.getThemeMode(),
        settingsRepository.getUpstreamDns(),
        settingsRepository.getMaxAgeDays(),
        settingsRepository.getMaxRows()
    ) { theme, dns, maxAge, maxRows ->
        SettingsTuple(theme, dns, maxAge, maxRows)
    }

    private val totalLogCountFlow = flow {
        emit(0L)
        delay(400)
        dnsLogRepository.observeTotalCount().collect { count ->
            emit(count.toLong())
        }
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsFlow,
        totalLogCountFlow,
        isClearingLogs,
        isUpdatingBlocklist
    ) { settings, logCount, clearing, updating ->
        SettingsUiState(
            themeMode = settings.themeMode,
            upstreamDns = settings.upstreamDns,
            maxAgeDays = settings.maxAgeDays,
            maxRows = settings.maxRows,
            totalLogCount = logCount,
            blocklistCount = blocklistRepository.domainCount(),
            isClearingLogs = clearing,
            isUpdatingBlocklist = updating
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

    fun clearAllLogs() {
        viewModelScope.launch {
            isClearingLogs.value = true
            dnsLogRepository.clearHistory()
            isClearingLogs.value = false
        }
    }
}

data class SettingsTuple(
    val themeMode: String,
    val upstreamDns: String,
    val maxAgeDays: Int,
    val maxRows: Int,
)
