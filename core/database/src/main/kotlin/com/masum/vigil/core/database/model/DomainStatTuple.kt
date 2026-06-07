package com.masum.vigil.core.database.model

data class DomainStatTuple(
    val domain: String,
    val count: Int,
    val isTracker: Int,
)
