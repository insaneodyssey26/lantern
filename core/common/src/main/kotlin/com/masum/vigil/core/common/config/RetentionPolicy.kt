package com.masum.vigil.core.common.config

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

/**
 * Rolling cap for persisted DNS history. Pruning removes rows that exceed *either* bound,
 * keeping the local database bounded for battery/IO and privacy reasons.
 */
data class RetentionPolicy(
    val maxAge: Duration = 7.days,
    val maxRows: Int = 50_000,
) {
    companion object {
        val DEFAULT = RetentionPolicy()
    }
}
