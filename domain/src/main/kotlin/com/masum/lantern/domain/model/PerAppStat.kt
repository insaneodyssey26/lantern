package com.masum.lantern.domain.model

data class PerAppStat(
    val appInfo: AppInfo,
    val totalQueries: Int,
    val trackerQueries: Int,
    val blockedQueries: Int = 0,
) {
    val trackerPercentage: Float
        get() = if (totalQueries == 0) 0f else trackerQueries / totalQueries.toFloat()
    val auditedQueries: Int
        get() = (trackerQueries - blockedQueries).coerceAtLeast(0)
}
