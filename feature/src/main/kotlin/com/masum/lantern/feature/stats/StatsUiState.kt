package com.masum.lantern.feature.stats

data class StatsUiState(
    val totalQueries: Int = 0,
    val trackerQueries: Int = 0,
    val trackerPercentage: Int = 0,
    val topDomains: List<DomainStatUiModel> = emptyList(),
    val blocklistCount: Int = 0,
    val apps: List<AppStatUiModel> = emptyList(),
    val sessions: List<SessionUiModel> = emptyList(),
)

data class DomainStatUiModel(
    val domain: String,
    val count: Int,
    val isTracker: Boolean,
)

data class AppStatUiModel(
    val appName: String,
    val packageName: String,
    val totalQueries: Int,
    val trackerQueries: Int,
    val trackerPercentage: Int,
)

data class SessionUiModel(
    val sessionId: Long,
    val startedAt: String,
    val duration: String,
    val totalQueries: Int,
    val trackerQueries: Int,
    val trackerPercentage: Int,
    val isActive: Boolean,
)
