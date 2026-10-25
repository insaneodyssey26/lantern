package com.masum.lantern.vpn.cache

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class DnsResponseCache(
    private val maxEntries: Int = 2048,
    private val defaultTtlSeconds: Long = 300L,
    private val minTtlSeconds: Long = 60L,
    private val maxTtlSeconds: Long = 86400L,
) {
    private data class CacheEntry(
        val responsePayload: ByteArray,
        val expiresAtMs: Long,
        var lastAccessedMs: Long,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is CacheEntry) return false
            return expiresAtMs == other.expiresAtMs && responsePayload.contentEquals(other.responsePayload)
        }

        override fun hashCode(): Int = responsePayload.contentHashCode()
    }

    private val map = ConcurrentHashMap<String, CacheEntry>(maxEntries)
    private val entryCount = AtomicInteger(0)

    fun get(domain: String, queryType: Int, transactionId: Int): ByteArray? {
        val key = cacheKey(domain, queryType)
        val entry = map[key] ?: return null
        val now = System.currentTimeMillis()

        if (now > entry.expiresAtMs) {
            map.remove(key)
            entryCount.decrementAndGet()
            return null
        }

        entry.lastAccessedMs = now
        val copy = entry.responsePayload.clone()
        if (copy.size >= 2) {
            copy[0] = (transactionId ushr 8).toByte()
            copy[1] = (transactionId and 0xFF).toByte()
        }
        return copy
    }

    fun put(domain: String, queryType: Int, dnsResponsePayload: ByteArray) {
        if (dnsResponsePayload.size < 12) return

        val rcode = dnsResponsePayload[3].toInt() and 0x0F
        if (rcode != 0) return

        val ttlSeconds = extractMinimumTtl(dnsResponsePayload).coerceIn(minTtlSeconds, maxTtlSeconds)
        val now = System.currentTimeMillis()
        val expiresAt = now + (ttlSeconds * 1000L)
        val key = cacheKey(domain, queryType)

        if (map.put(key, CacheEntry(dnsResponsePayload.clone(), expiresAt, now)) == null) {
            if (entryCount.incrementAndGet() > maxEntries) {
                evictOldest()
            }
        }
    }

    fun clear() {
        map.clear()
        entryCount.set(0)
    }

    fun size(): Int = map.size

    private fun cacheKey(domain: String, queryType: Int): String =
        "${domain.trim().lowercase()}:$queryType"

    private fun evictOldest() {
        val now = System.currentTimeMillis()
        val expiredKeys = mutableListOf<String>()

        for ((k, entry) in map) {
            if (now > entry.expiresAtMs) {
                expiredKeys.add(k)
            }
        }

        for (k in expiredKeys) {
            if (map.remove(k) != null) {
                entryCount.decrementAndGet()
            }
        }

        if (entryCount.get() > maxEntries) {
            val toRemove = map.entries
                .sortedBy { it.value.lastAccessedMs }
                .take(maxEntries / 4)
            for (entry in toRemove) {
                if (map.remove(entry.key) != null) {
                    entryCount.decrementAndGet()
                }
            }
        }
    }

    private fun extractMinimumTtl(dnsResponse: ByteArray): Long {
        return runCatching {
            val anCount = ((dnsResponse[6].toInt() and 0xFF) shl 8) or (dnsResponse[7].toInt() and 0xFF)
            if (anCount == 0) return defaultTtlSeconds

            val qdCount = ((dnsResponse[4].toInt() and 0xFF) shl 8) or (dnsResponse[5].toInt() and 0xFF)
            var pos = 12

            for (i in 0 until qdCount) {
                pos = skipDomainName(dnsResponse, pos) ?: return defaultTtlSeconds
                pos += 4
                if (pos > dnsResponse.size) return defaultTtlSeconds
            }

            var minTtl = Long.MAX_VALUE

            for (i in 0 until anCount) {
                pos = skipDomainName(dnsResponse, pos) ?: return defaultTtlSeconds
                if (pos + 10 > dnsResponse.size) return defaultTtlSeconds
                val ttl = ((dnsResponse[pos + 4].toLong() and 0xFF) shl 24) or
                    ((dnsResponse[pos + 5].toLong() and 0xFF) shl 16) or
                    ((dnsResponse[pos + 6].toLong() and 0xFF) shl 8) or
                    (dnsResponse[pos + 7].toLong() and 0xFF)
                if (ttl in 1 until minTtl) {
                    minTtl = ttl
                }
                val rdLength = ((dnsResponse[pos + 8].toInt() and 0xFF) shl 8) or (dnsResponse[pos + 9].toInt() and 0xFF)
                pos += 10 + rdLength
                if (pos > dnsResponse.size) break
            }

            if (minTtl == Long.MAX_VALUE) defaultTtlSeconds else minTtl
        }.getOrDefault(defaultTtlSeconds)
    }

    private fun skipDomainName(data: ByteArray, startPos: Int): Int? {
        var pos = startPos
        while (pos < data.size) {
            val len = data[pos].toInt() and 0xFF
            when {
                len == 0 -> return pos + 1
                len and 0xC0 == 0xC0 -> return pos + 2
                else -> pos += 1 + len
            }
        }
        return null
    }
}
