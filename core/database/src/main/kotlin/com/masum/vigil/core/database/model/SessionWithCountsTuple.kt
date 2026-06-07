package com.masum.vigil.core.database.model

data class SessionWithCountsTuple(
    val sessionId: Long,
    val startedAt: Long,
    val endedAt: Long?,
    val totalQueries: Int,
    val trackerQueries: Int,
)
