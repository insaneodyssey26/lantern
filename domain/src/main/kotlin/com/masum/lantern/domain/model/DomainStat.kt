package com.masum.lantern.domain.model

data class DomainStat(
    val domain: String,
    val count: Int,
    val isTracker: Boolean,
)
