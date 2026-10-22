package com.masum.lantern.feature.stats

data class StatsUiState(
    val totalQueries: Int = 0,
    val trackerQueries: Int = 0,
    val trackerPercentage: Int = 0,
    val topDomains: List<DomainStatUiModel> = emptyList(),
    val blocklistCount: Int = 0,
)

data class DomainStatUiModel(
    val domain: String,
    val count: Int,
    val isTracker: Boolean,
)
