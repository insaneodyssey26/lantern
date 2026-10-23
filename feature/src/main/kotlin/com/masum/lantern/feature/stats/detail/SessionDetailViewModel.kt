package com.masum.lantern.feature.stats.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masum.lantern.domain.model.DnsQuery
import com.masum.lantern.domain.model.PerAppStat
import com.masum.lantern.domain.model.SessionSummary
import com.masum.lantern.domain.model.Verdict
import com.masum.lantern.domain.repository.DnsLogRepository
import com.masum.lantern.feature.util.formatDate
import com.masum.lantern.feature.util.formatDuration
import com.masum.lantern.feature.util.formatTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SessionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dnsLogRepository: DnsLogRepository
) : ViewModel() {

    private val sessionId: Long = checkNotNull(savedStateHandle.get<Long>("sessionId") ?: savedStateHandle.get<String>("sessionId")?.toLongOrNull() ?: 0L)
    private val selectedTab = MutableStateFlow(SessionDetailTab.QUERIES)

    val uiState: StateFlow<SessionDetailUiState> = combine(
        dnsLogRepository.observeSession(sessionId),
        dnsLogRepository.observePerAppStats(sessionId),
        dnsLogRepository.observeSessionQueries(sessionId),
        selectedTab
    ) { session, apps, queries, tab ->
        val now = System.currentTimeMillis()
        if (session == null) {
            SessionDetailUiState(
                sessionId = sessionId,
                isLoading = false,
                selectedTab = tab
            )
        } else {
            SessionDetailUiState(
                sessionId = session.sessionId,
                startedAt = session.startedAt.formatDate(),
                duration = if (session.isActive) "Active" else formatDuration(session.startedAt, session.endedAt ?: now),
                isActive = session.isActive,
                totalQueries = session.totalQueries,
                trackerQueries = session.trackerQueries,
                trackerPercentage = (session.trackerPercentage * 100).toInt(),
                apps = apps.map { it.toUiModel() },
                queries = queries.map { it.toUiModel() },
                selectedTab = tab,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SessionDetailUiState(sessionId = sessionId, isLoading = true)
    )

    fun onTabSelected(tab: SessionDetailTab) {
        selectedTab.value = tab
    }

    fun getSessionAppQueries(packageName: String): kotlinx.coroutines.flow.Flow<List<DnsQuery>> =
        dnsLogRepository.observeSessionAppQueries(sessionId, packageName)

    private fun PerAppStat.toUiModel() = SessionAppStatUiModel(
        appName = appInfo.appName,
        packageName = appInfo.packageName,
        totalQueries = totalQueries,
        trackerQueries = trackerQueries,
        trackerPercentage = (trackerPercentage * 100).toInt()
    )

    private fun DnsQuery.toUiModel() = SessionQueryUiModel(
        id = id,
        domain = domain,
        timestamp = timestamp,
        formattedTime = timestamp.formatTime(),
        isTracker = verdict is Verdict.Tracker,
        isBlocked = isBlocked,
        appName = appInfo.appName,
        packageName = appInfo.packageName
    )
}
