package com.masum.lantern.feature.stats.detail

data class SessionDetailUiState(
    val sessionId: Long = 0L,
    val startedAt: String = "",
    val duration: String = "",
    val isActive: Boolean = false,
    val totalQueries: Int = 0,
    val trackerQueries: Int = 0,
    val trackerPercentage: Int = 0,
    val apps: List<SessionAppStatUiModel> = emptyList(),
    val queries: List<SessionQueryUiModel> = emptyList(),
    val selectedTab: SessionDetailTab = SessionDetailTab.QUERIES,
    val isLoading: Boolean = true
)

enum class SessionDetailTab {
    QUERIES,
    APPS
}

data class SessionAppStatUiModel(
    val appName: String,
    val packageName: String,
    val totalQueries: Int,
    val trackerQueries: Int,
    val trackerPercentage: Int
)

data class SessionQueryUiModel(
    val id: Long,
    val domain: String,
    val timestamp: Long,
    val formattedTime: String,
    val isTracker: Boolean,
    val isBlocked: Boolean,
    val appName: String,
    val packageName: String
)
