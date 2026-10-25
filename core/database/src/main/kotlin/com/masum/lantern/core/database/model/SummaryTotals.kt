package com.masum.lantern.core.database.model

data class SummaryTotals(
    val totalQueries: Int,
    val trackerQueries: Int,
    val blockedQueries: Int = 0,
)
