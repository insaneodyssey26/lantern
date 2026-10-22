package com.masum.lantern.feature.control

import android.content.Context
import android.net.ConnectivityManager
import android.net.VpnService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masum.lantern.domain.model.MonitoringState
import com.masum.lantern.domain.repository.BlocklistRepository
import com.masum.lantern.domain.repository.SettingsRepository
import com.masum.lantern.domain.usecase.GetSummaryStatsUseCase
import com.masum.lantern.domain.usecase.ObserveMonitoringStateUseCase
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
    private val observeMonitoringState: ObserveMonitoringStateUseCase,
    private val getSummaryStats: GetSummaryStatsUseCase,
    private val blocklistRepository: BlocklistRepository,
    private val settingsRepository: SettingsRepository,
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
                _uiState.update { it.copy(totalQueries = stats.totalQueries, trackerQueries = stats.trackerQueries) }
            }
        }
        viewModelScope.launch {
            settingsRepository.isBlockingEnabled().collect { enabled ->
                _uiState.update { it.copy(isBlockingEnabled = enabled) }
            }
        }
        refreshPrivateDnsStatus()
    }

    fun setBlockingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBlockingEnabled(enabled)
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
}
