package com.masum.vigil.vpn.parser

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class DnsPacketBuilderTest {

    private val fakeDnsResponse = byteArrayOf(
        0x12, 0x34,
        0x81.toByte(), 0x80.toByte(),
        0x00, 0x01,
        0x00, 0x01,
        0x00, 0x00,
        0x00, 0x00,
    )

    @Test
    fun `wraps response with correct ip and udp structure`() {
        val srcIp = (10 shl 24) or (0 shl 16) or (0 shl 8) or 53
        val dstIp = (10 shl 24) or (0 shl 16) or (0 shl 8) or 2
        val packet = DnsPacketBuilder.wrapResponse(
            dnsResponse = fakeDnsResponse,
            sourceIp = srcIp,
            destIp = dstIp,
            sourcePort = 53,
            destPort = 5353,
        )

        val expectedTotal = 20 + 8 + fakeDnsResponse.size
        assertEquals(expectedTotal, packet.size)

        assertEquals(0x45, packet[0].toInt() and 0xFF)
        assertEquals(0x11, packet[9].toInt() and 0xFF)

        val totalLen = DnsPacketParser.readUShort(packet, 2)
        assertEquals(expectedTotal, totalLen)

        val destPort = DnsPacketParser.readUShort(packet, 22)
        assertEquals(5353, destPort)

        val srcPort = DnsPacketParser.readUShort(packet, 20)
        assertEquals(53, srcPort)
    }

    @Test
    fun `dns payload is preserved verbatim`() {
        val packet = DnsPacketBuilder.wrapResponse(
            dnsResponse = fakeDnsResponse,
            sourceIp = 0,
            destIp = 0,
            sourcePort = 53,
            destPort = 1234,
        )
        val payload = packet.copyOfRange(28, 28 + fakeDnsResponse.size)
        assertArrayEquals(fakeDnsResponse, payload)
    }

    @Test
    fun `ip checksum field is non-zero for non-trivial header`() {
        val packet = DnsPacketBuilder.wrapResponse(
            dnsResponse = fakeDnsResponse,
            sourceIp = (1 shl 24) or (1 shl 16) or (1 shl 8) or 1,
            destIp = (10 shl 24) or (0 shl 16) or (0 shl 8) or 2,
            sourcePort = 53,
            destPort = 4321,
        )
        val checksum = DnsPacketParser.readUShort(packet, 10)
        assert(checksum != 0) { "checksum should be non-zero for a real header" }
    }
}
