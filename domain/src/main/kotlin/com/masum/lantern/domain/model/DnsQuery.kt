package com.masum.lantern.domain.model

data class DnsQuery(
    val id: Long = 0,
    val sessionId: Long,
    val domain: String,
    val timestamp: Long,
    val verdict: Verdict,
    val appInfo: AppInfo,
    val isBlocked: Boolean = false,
)
