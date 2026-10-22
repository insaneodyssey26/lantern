package com.masum.lantern.domain.model

data class SummaryStats(
    val totalQueries: Int,
    val trackerQueries: Int,
    val topDomains: List<DomainStat>,
) {
    val trackerPercentage: Float
        get() = if (totalQueries == 0) 0f else trackerQueries / totalQueries.toFloat()

    companion object {
        val EMPTY = SummaryStats(totalQueries = 0, trackerQueries = 0, topDomains = emptyList())
    }
}
