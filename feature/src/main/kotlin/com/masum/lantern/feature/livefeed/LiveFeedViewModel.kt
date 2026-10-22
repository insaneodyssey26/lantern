package com.masum.lantern.feature.livefeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masum.lantern.domain.model.DnsQuery
import com.masum.lantern.domain.model.MonitoringState
import com.masum.lantern.domain.repository.SettingsRepository
import com.masum.lantern.domain.usecase.ObserveLiveFeedUseCase
import com.masum.lantern.domain.usecase.ObserveMonitoringStateUseCase
import com.masum.lantern.feature.util.formatTime
import com.masum.lantern.feature.util.toUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

@HiltViewModel
class LiveFeedViewModel @Inject constructor(
    private val observeLiveFeed: ObserveLiveFeedUseCase,
    private val observeMonitoringState: ObserveMonitoringStateUseCase,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveFeedUiState())
    val uiState: StateFlow<LiveFeedUiState> = _uiState.asStateFlow()

    private val queryBuffer = ArrayDeque<DnsQueryUiModel>(MAX_ITEMS + 1)
    private val localIdCounter = AtomicLong(0)

    init {
        viewModelScope.launch {
            observeMonitoringState().collect { state ->
                _uiState.update { it.copy(isMonitoring = state is MonitoringState.Active) }
            }
        }
        viewModelScope.launch {
            settingsRepository.getUserAllowedDomains().collect { allowed ->
                _uiState.update { it.copy(userAllowedDomains = allowed) }
            }
        }
        viewModelScope.launch {
            settingsRepository.getUserBlockedDomains().collect { blocked ->
                _uiState.update { it.copy(userBlockedDomains = blocked) }
            }
        }
        viewModelScope.launch {
            observeLiveFeed().collect { query ->
                if (_uiState.value.isPaused) return@collect
                queryBuffer.addFirst(query.toUiModel())
                if (queryBuffer.size > MAX_ITEMS) queryBuffer.removeLast()
                val snapshot: ImmutableList<DnsQueryUiModel> = queryBuffer.toPersistentList()
                _uiState.update { it.copy(queries = snapshot) }
            }
        }
    }

    fun togglePauseStream() {
        _uiState.update { it.copy(isPaused = !it.isPaused) }
    }

    fun clearFeed() {
        queryBuffer.clear()
        _uiState.update { it.copy(queries = persistentListOf()) }
    }

    fun allowDomain(domain: String) {
        viewModelScope.launch {
            settingsRepository.setUserDomainAllowed(domain, true)
        }
    }

    fun blockDomain(domain: String) {
        viewModelScope.launch {
            settingsRepository.setUserDomainBlocked(domain, true)
        }
    }

    fun removeDomainRule(domain: String) {
        viewModelScope.launch {
            settingsRepository.setUserDomainAllowed(domain, false)
            settingsRepository.setUserDomainBlocked(domain, false)
        }
    }

    private fun DnsQuery.toUiModel() = DnsQueryUiModel(
        id = localIdCounter.incrementAndGet(),
        domain = domain,
        appName = appInfo.appName,
        packageName = appInfo.packageName,
        verdict = verdict.toUiState(),
        time = timestamp.formatTime(),
        isBlocked = isBlocked,
    )

    companion object {
        private const val MAX_ITEMS = 100
    }
}
