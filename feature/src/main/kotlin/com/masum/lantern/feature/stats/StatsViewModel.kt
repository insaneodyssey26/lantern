package com.masum.lantern.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masum.lantern.domain.model.SummaryStats
import com.masum.lantern.domain.repository.BlocklistRepository
import com.masum.lantern.domain.usecase.GetSummaryStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    getSummaryStats: GetSummaryStatsUseCase,
    private val blocklistRepository: BlocklistRepository,
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = getSummaryStats()
        .map { it.toUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatsUiState(),
        )

    private fun SummaryStats.toUiState() = StatsUiState(
        totalQueries = totalQueries,
        trackerQueries = trackerQueries,
        trackerPercentage = (trackerPercentage * 100).toInt(),
        topDomains = topDomains.map { DomainStatUiModel(it.domain, it.count, it.isTracker) },
        blocklistCount = blocklistRepository.domainCount(),
    )
}
