package com.masum.lantern.domain.model

data class SessionSummary(
    val sessionId: Long,
    val startedAt: Long,
    val endedAt: Long?,
    val totalQueries: Int,
    val trackerQueries: Int,
    val blockedQueries: Int = 0,
) {
    val trackerPercentage: Float
        get() = if (totalQueries == 0) 0f else trackerQueries / totalQueries.toFloat()

    val auditedQueries: Int
        get() = (trackerQueries - blockedQueries).coerceAtLeast(0)

    val isActive: Boolean get() = endedAt == null
}
