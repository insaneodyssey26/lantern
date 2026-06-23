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
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveFeedUiState())
    val uiState: StateFlow<LiveFeedUiState> = _uiState.asStateFlow()

    // Plain mutable buffer; we convert to ImmutableList before exposing to UI
    private val queryBuffer = ArrayDeque<DnsQueryUiModel>(MAX_ITEMS + 1)
    private val localIdCounter = AtomicLong(0)

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
                // toPersistentList() gives Compose a Stable reference — the LazyColumn can
                // skip recomposing items that haven't changed rather than redrawing everything.
                val snapshot: ImmutableList<DnsQueryUiModel> = queryBuffer.toPersistentList()
                _uiState.update { it.copy(queries = snapshot) }
            }
        }
    }

    private fun DnsQuery.toUiModel() = DnsQueryUiModel(
        id = localIdCounter.incrementAndGet(),
        domain = domain,
        appName = appInfo.appName,
        packageName = appInfo.packageName,
        verdict = verdict.toUiState(),
        time = timestamp.formatTime(),
    )

    companion object {
        private const val MAX_ITEMS = 100
    }
}
