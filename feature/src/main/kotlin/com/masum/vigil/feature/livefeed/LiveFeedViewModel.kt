package com.masum.vigil.feature.livefeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masum.vigil.domain.model.DnsQuery
import com.masum.vigil.domain.model.MonitoringState
import com.masum.vigil.domain.usecase.ObserveLiveFeedUseCase
import com.masum.vigil.domain.usecase.ObserveMonitoringStateUseCase
import com.masum.vigil.feature.util.formatTime
import com.masum.vigil.feature.util.toUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LiveFeedViewModel @Inject constructor(
    private val observeLiveFeed: ObserveLiveFeedUseCase,
    private val observeMonitoringState: ObserveMonitoringStateUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveFeedUiState())
    val uiState: StateFlow<LiveFeedUiState> = _uiState.asStateFlow()

    private val queryBuffer = ArrayDeque<DnsQueryUiModel>(MAX_ITEMS + 1)

    init {
        viewModelScope.launch {
            observeMonitoringState().collect { state ->
                _uiState.update { it.copy(isMonitoring = state is MonitoringState.Active) }
            }
        }
        viewModelScope.launch {
            observeLiveFeed().collect { query ->
                queryBuffer.addFirst(query.toUiModel())
                if (queryBuffer.size > MAX_ITEMS) queryBuffer.removeLast()
                _uiState.update { it.copy(queries = queryBuffer.toList()) }
            }
        }
    }

    private fun DnsQuery.toUiModel() = DnsQueryUiModel(
        id = id,
        domain = domain,
        appName = appInfo.appName,
        verdict = verdict.toUiState(),
        time = timestamp.formatTime(),
    )

    companion object {
        private const val MAX_ITEMS = 100
    }
}
