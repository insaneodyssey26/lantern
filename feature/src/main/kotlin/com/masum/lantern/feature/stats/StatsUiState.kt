package com.masum.lantern.feature.stats

data class StatsUiState(
    val totalQueries: Int = 0,
    val trackerQueries: Int = 0,
    val blockedQueries: Int = 0,
    val trackerPercentage: Int = 0,
    val topDomains: List<DomainStatUiModel> = emptyList(),
    val blocklistCount: Int = 0,
    val apps: List<AppStatUiModel> = emptyList(),
    val sessions: List<SessionUiModel> = emptyList(),
) {
    val cleanQueries: Int get() = (totalQueries - trackerQueries).coerceAtLeast(0)
    val auditedQueries: Int get() = (trackerQueries - blockedQueries).coerceAtLeast(0)
}

data class DomainStatUiModel(
    val domain: String,
    val count: Int,
    val isTracker: Boolean,
    val isBlocked: Boolean = false,
)

data class AppStatUiModel(
    val appName: String,
    val packageName: String,
    val totalQueries: Int,
    val trackerQueries: Int,
    val blockedQueries: Int = 0,
    val trackerPercentage: Int,
) {
    val auditedQueries: Int get() = (trackerQueries - blockedQueries).coerceAtLeast(0)
}

data class SessionUiModel(
    val sessionId: Long,
    val startedAt: String,
    val duration: String,
    val totalQueries: Int,
    val trackerQueries: Int,
    val blockedQueries: Int = 0,
    val trackerPercentage: Int,
    val isActive: Boolean,
) {
    val auditedQueries: Int get() = (trackerQueries - blockedQueries).coerceAtLeast(0)
}
