package com.masum.lantern.core.common.config

object ResolverConfig {
    const val PRIMARY_DNS: String = "1.1.1.1"
    const val FALLBACK_DNS: String = "9.9.9.9"
    const val GOOGLE_DNS: String = "8.8.8.8"
    const val DNS_PORT: Int = 53
    const val DOH_PORT: Int = 443
    const val DOT_PORT: Int = 853
    const val QUERY_TIMEOUT_MS: Long = 5_000L

    fun getSniHostname(upstreamIp: String): String = when (upstreamIp) {
        PRIMARY_DNS -> "cloudflare-dns.com"
        FALLBACK_DNS -> "dns.quad9.net"
        GOOGLE_DNS -> "dns.google"
        else -> "cloudflare-dns.com"
    }

    fun getDohUrl(upstreamIp: String): String = when (upstreamIp) {
        PRIMARY_DNS -> "https://cloudflare-dns.com/dns-query"
        FALLBACK_DNS -> "https://dns.quad9.net/dns-query"
        GOOGLE_DNS -> "https://dns.google/dns-query"
        else -> "https://cloudflare-dns.com/dns-query"
    }

    fun getDohBootstrapIps(upstreamIp: String): List<String> = when (upstreamIp) {
        PRIMARY_DNS -> listOf("1.1.1.1", "1.0.0.1")
        FALLBACK_DNS -> listOf("9.9.9.9", "149.112.112.112")
        GOOGLE_DNS -> listOf("8.8.8.8", "8.8.4.4")
        else -> listOf(upstreamIp)
    }
}
