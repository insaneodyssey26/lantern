package com.masum.lantern.domain.model

data class SummaryStats(
    val totalQueries: Int,
    val trackerQueries: Int,
    val blockedQueries: Int = 0,
    val topDomains: List<DomainStat>,
) {
    val trackerPercentage: Float
        get() = if (totalQueries == 0) 0f else trackerQueries / totalQueries.toFloat()

    val auditedQueries: Int
        get() = (trackerQueries - blockedQueries).coerceAtLeast(0)

    companion object {
        val EMPTY = SummaryStats(totalQueries = 0, trackerQueries = 0, blockedQueries = 0, topDomains = emptyList())
    }
}
