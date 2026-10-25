package com.masum.lantern.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masum.lantern.domain.model.DnsQuery
import com.masum.lantern.domain.model.PerAppStat
import com.masum.lantern.domain.model.SessionSummary
import com.masum.lantern.domain.model.SummaryStats
import com.masum.lantern.domain.repository.BlocklistRepository
import com.masum.lantern.domain.repository.DnsLogRepository
import com.masum.lantern.domain.usecase.ClearHistoryUseCase
import com.masum.lantern.domain.usecase.GetPerAppStatsUseCase
import com.masum.lantern.domain.usecase.GetSessionHistoryUseCase
import com.masum.lantern.domain.usecase.GetSummaryStatsUseCase
import com.masum.lantern.feature.util.formatDate
import com.masum.lantern.feature.util.formatDuration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    getSummaryStats: GetSummaryStatsUseCase,
    getPerAppStats: GetPerAppStatsUseCase,
    getSessionHistory: GetSessionHistoryUseCase,
    private val blocklistRepository: BlocklistRepository,
    private val dnsLogRepository: DnsLogRepository,
    private val clearHistoryUseCase: ClearHistoryUseCase,
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        getSummaryStats(),
        getPerAppStats(),
        getSessionHistory()
    ) { summary, apps, sessions ->
        val now = System.currentTimeMillis()
        StatsUiState(
            totalQueries = summary.totalQueries,
            trackerQueries = summary.trackerQueries,
            blockedQueries = summary.blockedQueries,
            trackerPercentage = (summary.trackerPercentage * 100).toInt(),
            topDomains = summary.topDomains.map {
                DomainStatUiModel(
                    domain = it.domain,
                    count = it.count,
                    isTracker = it.isTracker,
                    isBlocked = it.isBlocked,
                )
            },
            blocklistCount = blocklistRepository.domainCount(),
            apps = apps.map { it.toUiModel() },
            sessions = sessions.map { it.toUiModel(now) }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState()
    )

    fun getAppQueries(packageName: String): Flow<List<DnsQuery>> =
        dnsLogRepository.observeAppQueries(packageName)

    fun onClearHistory() {
        viewModelScope.launch {
            clearHistoryUseCase()
        }
    }

    private fun PerAppStat.toUiModel() = AppStatUiModel(
        appName = appInfo.appName,
        packageName = appInfo.packageName,
        totalQueries = totalQueries,
        trackerQueries = trackerQueries,
        blockedQueries = blockedQueries,
        trackerPercentage = (trackerPercentage * 100).toInt(),
    )

    private fun SessionSummary.toUiModel(now: Long) = SessionUiModel(
        sessionId = sessionId,
        startedAt = startedAt.formatDate(),
        duration = if (isActive) "Active" else formatDuration(startedAt, endedAt ?: now),
        totalQueries = totalQueries,
        trackerQueries = trackerQueries,
        blockedQueries = blockedQueries,
        trackerPercentage = (trackerPercentage * 100).toInt(),
        isActive = isActive,
    )
}
