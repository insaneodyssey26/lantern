package com.masum.vigil.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masum.vigil.domain.model.SessionSummary
import com.masum.vigil.domain.usecase.ClearHistoryUseCase
import com.masum.vigil.domain.usecase.GetSessionHistoryUseCase
import com.masum.vigil.feature.util.formatDate
import com.masum.vigil.feature.util.formatDuration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    getSessionHistory: GetSessionHistoryUseCase,
    private val clearHistoryUseCase: ClearHistoryUseCase,
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = getSessionHistory()
        .map { sessions -> HistoryUiState(sessions = sessions.map { it.toUiModel() }) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState(),
        )

    fun onClearHistory() {
        viewModelScope.launch { clearHistoryUseCase() }
    }

    private fun SessionSummary.toUiModel(): SessionSummaryUiModel {
        val now = System.currentTimeMillis()
        return SessionSummaryUiModel(
            sessionId = sessionId,
            startedAt = startedAt.formatDate(),
            duration = if (isActive) "Active" else formatDuration(startedAt, endedAt ?: now),
            totalQueries = totalQueries,
            trackerQueries = trackerQueries,
            trackerPercentage = (trackerPercentage * 100).toInt(),
            isActive = isActive,
        )
    }
}
