package com.masum.lantern.core.common.config

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

data class RetentionPolicy(
    val maxAge: Duration = 7.days,
    val maxRows: Int = 50_000,
) {
    companion object {
        val DEFAULT = RetentionPolicy()
    }
}
