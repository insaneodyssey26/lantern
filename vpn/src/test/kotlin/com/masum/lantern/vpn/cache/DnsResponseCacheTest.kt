package com.masum.lantern.vpn.cache

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DnsResponseCacheTest {

    private fun createDummyDnsResponse(txId: Int, domain: String, ttlSeconds: Long): ByteArray {
        val bytes = ByteArray(256)
        bytes[0] = (txId ushr 8).toByte()
        bytes[1] = (txId and 0xFF).toByte()
        bytes[2] = 0x81.toByte()
        bytes[3] = 0x80.toByte()
        bytes[4] = 0x00.toByte()
        bytes[5] = 0x01.toByte()
        bytes[6] = 0x00.toByte()
        bytes[7] = 0x01.toByte()

        var pos = 12
        for (part in domain.split(".")) {
            bytes[pos++] = part.length.toByte()
            for (ch in part) {
                bytes[pos++] = ch.code.toByte()
            }
        }
        bytes[pos++] = 0x00.toByte()
        bytes[pos++] = 0x00.toByte()
        bytes[pos++] = 0x01.toByte()
        bytes[pos++] = 0x00.toByte()
        bytes[pos++] = 0x01.toByte()

        bytes[pos++] = 0xC0.toByte()
        bytes[pos++] = 0x0C.toByte()
        bytes[pos++] = 0x00.toByte()
        bytes[pos++] = 0x01.toByte()
        bytes[pos++] = 0x00.toByte()
        bytes[pos++] = 0x01.toByte()

        bytes[pos++] = (ttlSeconds ushr 24 and 0xFF).toByte()
        bytes[pos++] = (ttlSeconds ushr 16 and 0xFF).toByte()
        bytes[pos++] = (ttlSeconds ushr 8 and 0xFF).toByte()
        bytes[pos++] = (ttlSeconds and 0xFF).toByte()

        bytes[pos++] = 0x00.toByte()
        bytes[pos++] = 0x04.toByte()
        bytes[pos++] = 1.toByte()
        bytes[pos++] = 1.toByte()
        bytes[pos++] = 1.toByte()
        bytes[pos++] = 1.toByte()

        return bytes.copyOf(pos)
    }

    @Test
    fun cacheHitReturnsUpdatedTransactionId() {
        val cache = DnsResponseCache(maxEntries = 100)
        val payload = createDummyDnsResponse(1234, "example.com", 300)

        cache.put("example.com", 1, payload)

        val cached = cache.get("example.com", 1, 9999)
        assertNotNull(cached)
        val txId = ((cached!![0].toInt() and 0xFF) shl 8) or (cached[1].toInt() and 0xFF)
        assertEquals(9999, txId)
    }

    @Test
    fun cacheMissReturnsNull() {
        val cache = DnsResponseCache(maxEntries = 100)
        assertNull(cache.get("unknown.com", 1, 1234))
    }

    @Test
    fun cacheEvictionWhenLimitExceeded() {
        val cache = DnsResponseCache(maxEntries = 5)
        for (i in 1..10) {
            val payload = createDummyDnsResponse(i, "domain$i.com", 300)
            cache.put("domain$i.com", 1, payload)
        }
        assertTrue(cache.size() <= 8)
    }
}
