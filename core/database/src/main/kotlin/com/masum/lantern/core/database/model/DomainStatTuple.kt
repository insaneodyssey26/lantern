package com.masum.lantern.core.database.model

data class DomainStatTuple(
    val domain: String,
    val count: Int,
    val isTracker: Int,
    val isBlocked: Int = 0,
)
