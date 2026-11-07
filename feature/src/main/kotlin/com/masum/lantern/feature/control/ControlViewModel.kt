package com.masum.lantern.feature.control

import android.content.Context
import android.net.ConnectivityManager
import android.net.VpnService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masum.lantern.domain.model.MonitoringState
import com.masum.lantern.domain.repository.BlocklistRepository
import com.masum.lantern.domain.repository.DnsBenchmarkRepository
import com.masum.lantern.domain.repository.SettingsRepository
import com.masum.lantern.domain.usecase.GetSummaryStatsUseCase
import com.masum.lantern.domain.usecase.ObserveMonitoringStateUseCase
import com.masum.lantern.domain.usecase.PauseMonitoringUseCase
import com.masum.lantern.domain.usecase.ResumeMonitoringUseCase
import com.masum.lantern.domain.usecase.StartMonitoringUseCase
import com.masum.lantern.domain.usecase.StopMonitoringUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ControlViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val startMonitoring: StartMonitoringUseCase,
    private val stopMonitoring: StopMonitoringUseCase,
    private val pauseMonitoringUseCase: PauseMonitoringUseCase,
    private val resumeMonitoringUseCase: ResumeMonitoringUseCase,
    private val observeMonitoringState: ObserveMonitoringStateUseCase,
    private val getSummaryStats: GetSummaryStatsUseCase,
    private val blocklistRepository: BlocklistRepository,
    private val settingsRepository: SettingsRepository,
    private val dnsBenchmarkRepository: DnsBenchmarkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ControlUiState(blocklistCount = blocklistRepository.domainCount()))
    val uiState: StateFlow<ControlUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeMonitoringState().collect { state ->
                _uiState.update { it.copy(monitoringState = state) }
            }
        }
        viewModelScope.launch {
            getSummaryStats().collect { stats ->
                _uiState.update {
                    it.copy(
                        totalQueries = stats.totalQueries,
                        trackerQueries = stats.trackerQueries,
                        blockedQueries = stats.blockedQueries,
                    )
                }
            }
        }
        viewModelScope.launch {
            settingsRepository.isBlockingEnabled().collect { enabled ->
                _uiState.update { it.copy(isBlockingEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            settingsRepository.getUpstreamDns().collect { dns ->
                _uiState.update { it.copy(upstreamDns = dns) }
            }
        }
        viewModelScope.launch {
            settingsRepository.getDnsProtocol().collect { proto ->
                _uiState.update { it.copy(dnsProtocol = proto) }
            }
        }
        viewModelScope.launch {
            dnsBenchmarkRepository.isBenchmarking.collect { benchmarking ->
                _uiState.update { it.copy(isBenchmarking = benchmarking) }
            }
        }
        viewModelScope.launch {
            dnsBenchmarkRepository.benchmarkResults.collect { results ->
                _uiState.update { it.copy(benchmarkResults = results) }
            }
        }
        viewModelScope.launch {
            settingsRepository.isFeatureGuideCompleted().collect { completed ->
                _uiState.update { it.copy(isFeatureGuideCompleted = completed) }
            }
        }
        viewModelScope.launch {
            val lastSeen = settingsRepository.getLastSeenVersionCodeSync()
            if (lastSeen < 3) {
                _uiState.update { it.copy(showWhatsNewSheet = true) }
            }
        }
        refreshPrivateDnsStatus()
    }

    fun dismissWhatsNew() {
        _uiState.update { it.copy(showWhatsNewSheet = false) }
        viewModelScope.launch {
            settingsRepository.setLastSeenVersionCode(3)
        }
    }

    fun completeFeatureGuide() {
        viewModelScope.launch {
            settingsRepository.setFeatureGuideCompleted(true)
        }
    }

    fun setBlockingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBlockingEnabled(enabled)
        }
    }

    fun setUpstreamDns(dns: String) {
        viewModelScope.launch {
            settingsRepository.setUpstreamDns(dns)
        }
    }

    fun onToggleClick() {
        viewModelScope.launch {
            if (_uiState.value.monitoringState is MonitoringState.Idle) {
                val consentIntent = VpnService.prepare(context)
                if (consentIntent != null) {
                    _uiState.update { it.copy(pendingConsentIntent = consentIntent) }
                    return@launch
                }
                startMonitoring()
            } else {
                stopMonitoring()
            }
        }
    }

    fun onConsentGranted() {
        _uiState.update { it.copy(pendingConsentIntent = null) }
        viewModelScope.launch { startMonitoring() }
    }

    fun onConsentDismissed() {
        _uiState.update { it.copy(pendingConsentIntent = null) }
    }

    fun refreshPrivateDnsStatus() {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val lp = cm.getLinkProperties(cm.activeNetwork ?: return) ?: return
        _uiState.update { it.copy(isPrivateDnsActive = lp.isPrivateDnsActive) }
    }

    fun pauseProtection(durationMs: Long) {
        viewModelScope.launch {
            pauseMonitoringUseCase(durationMs)
        }
    }

    fun resumeProtection() {
        viewModelScope.launch {
            resumeMonitoringUseCase()
        }
    }

    fun runBenchmark(protocol: String) {
        viewModelScope.launch {
            dnsBenchmarkRepository.runBenchmark(protocol)
        }
    }

    fun stopBenchmark() {
        dnsBenchmarkRepository.stopBenchmark()
    }
}
