package com.masum.vigil.core.common.config

/**
 * Upstream DNS resolver configuration for the local relay.
 *
 * Vigil never routes general traffic anywhere — it only forwards the DNS queries it
 * captures to a fixed public resolver so that resolution keeps working while monitoring.
 */
object ResolverConfig {
    /** Primary upstream resolver (Cloudflare). */
    const val PRIMARY_DNS: String = "1.1.1.1"

    /** Fallback upstream resolver (Quad9) used if the primary is unreachable. */
    const val FALLBACK_DNS: String = "9.9.9.9"

    /** Standard DNS port. */
    const val DNS_PORT: Int = 53

    /** How long to wait for an upstream response before giving up on a forwarded query. */
    const val QUERY_TIMEOUT_MS: Long = 5_000L
}
