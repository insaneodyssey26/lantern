package com.masum.lantern.core.database.model

data class SessionWithCountsTuple(
    val sessionId: Long,
    val startedAt: Long,
    val endedAt: Long?,
    val totalQueries: Int,
    val trackerQueries: Int,
    val blockedQueries: Int = 0,
)
