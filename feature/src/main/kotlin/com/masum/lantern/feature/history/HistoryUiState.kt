package com.masum.lantern.feature.history

data class HistoryUiState(
    val sessions: List<SessionSummaryUiModel> = emptyList(),
)

data class SessionSummaryUiModel(
    val sessionId: Long,
    val startedAt: String,
    val duration: String,
    val totalQueries: Int,
    val trackerQueries: Int,
    val trackerPercentage: Int,
    val isActive: Boolean,
)
