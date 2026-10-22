package com.masum.lantern.core.common.config

object ResolverConfig {
    const val PRIMARY_DNS: String = "1.1.1.1"
    const val FALLBACK_DNS: String = "9.9.9.9"
    const val DNS_PORT: Int = 53
    const val QUERY_TIMEOUT_MS: Long = 5_000L
}
