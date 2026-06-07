package com.masum.vigil.vpn.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class DnsPacketParserTest {

    private fun buildPacket(
        domain: String,
        txId: Int = 0x1234,
        srcPort: Int = 5353,
        qType: Int = 1,
        srcIp: ByteArray = byteArrayOf(10, 0, 0, 2),
        dstIp: ByteArray = byteArrayOf(10, 0, 0, 53),
    ): ByteArray {
        val labels = domain.split(".")
        val qname = mutableListOf<Byte>()
        for (label in labels) {
            qname.add(label.length.toByte())
            qname.addAll(label.toByteArray(Charsets.US_ASCII).toList())
        }
        qname.add(0x00)

        val dns = ByteArray(12 + qname.size + 4)
        dns[0] = (txId ushr 8).toByte()
        dns[1] = (txId and 0xFF).toByte()
        dns[2] = 0x01; dns[3] = 0x00
        dns[4] = 0x00; dns[5] = 0x01
        dns[6] = 0x00; dns[7] = 0x00
        dns[8] = 0x00; dns[9] = 0x00
        dns[10] = 0x00; dns[11] = 0x00
        qname.forEachIndexed { i, b -> dns[12 + i] = b }
        val qoff = 12 + qname.size
        dns[qoff] = 0x00; dns[qoff + 1] = qType.toByte()
        dns[qoff + 2] = 0x00; dns[qoff + 3] = 0x01

        val udpLen = 8 + dns.size
        val totalLen = 20 + udpLen
        val packet = ByteArray(totalLen)

        packet[0] = 0x45
        packet[1] = 0x00
        packet[2] = (totalLen ushr 8).toByte()
        packet[3] = (totalLen and 0xFF).toByte()
        packet[4] = 0x00; packet[5] = 0x00
        packet[6] = 0x00; packet[7] = 0x00
        packet[8] = 0x40
        packet[9] = 0x11
        packet[10] = 0x00; packet[11] = 0x00
        srcIp.copyInto(packet, 12)
        dstIp.copyInto(packet, 16)

        packet[20] = (srcPort ushr 8).toByte()
        packet[21] = (srcPort and 0xFF).toByte()
        packet[22] = 0x00; packet[23] = 0x35
        packet[24] = (udpLen ushr 8).toByte()
        packet[25] = (udpLen and 0xFF).toByte()
        packet[26] = 0x00; packet[27] = 0x00

        dns.copyInto(packet, 28)
        return packet
    }

    @Test
    fun `parses simple A query for google dot com`() {
        val packet = buildPacket("google.com", txId = 0xABCD, qType = 1)
        val result = DnsPacketParser.parse(packet)
        assertNotNull(result)
        assertEquals("google.com", result!!.domain)
        assertEquals(0xABCD, result.transactionId)
        assertEquals(1, result.queryType)
    }

    @Test
    fun `parses AAAA query`() {
        val packet = buildPacket("example.com", qType = 28)
        val result = DnsPacketParser.parse(packet)
        assertNotNull(result)
        assertEquals(28, result!!.queryType)
    }

    @Test
    fun `parses multi-label subdomain`() {
        val packet = buildPacket("ads.tracking.example.com")
        val result = DnsPacketParser.parse(packet)
        assertNotNull(result)
        assertEquals("ads.tracking.example.com", result!!.domain)
    }

    @Test
    fun `parses single-label domain`() {
        val packet = buildPacket("localhost")
        val result = DnsPacketParser.parse(packet)
        assertNotNull(result)
        assertEquals("localhost", result!!.domain)
    }

    @Test
    fun `source ip and port are captured`() {
        val packet = buildPacket(
            "test.com",
            srcPort = 4321,
            srcIp = byteArrayOf(192.toByte(), 168.toByte(), 1, 100),
        )
        val result = DnsPacketParser.parse(packet)
        assertNotNull(result)
        assertEquals(4321, result!!.sourcePort)
        val expectedSrcIp = (192 shl 24) or (168 shl 16) or (1 shl 8) or 100
        assertEquals(expectedSrcIp, result.sourceIp)
    }

    @Test
    fun `returns null for packet too small`() {
        assertNull(DnsPacketParser.parse(ByteArray(10)))
    }

    @Test
    fun `returns null for empty packet`() {
        assertNull(DnsPacketParser.parse(ByteArray(0)))
    }

    @Test
    fun `returns null for non-UDP protocol`() {
        val packet = buildPacket("google.com")
        packet[9] = 0x06
        assertNull(DnsPacketParser.parse(packet))
    }

    @Test
    fun `returns null for non-DNS destination port`() {
        val packet = buildPacket("google.com")
        packet[22] = 0x01
        packet[23] = 0xBB.toByte()
        assertNull(DnsPacketParser.parse(packet))
    }

    @Test
    fun `returns null for DNS response flag set`() {
        val packet = buildPacket("google.com")
        packet[30] = 0x81.toByte()
        assertNull(DnsPacketParser.parse(packet))
    }

    @Test
    fun `returns null for malformed IPv4 version`() {
        val packet = buildPacket("google.com")
        packet[0] = 0x65
        assertNull(DnsPacketParser.parse(packet))
    }

    @Test
    fun `handles compression pointer`() {
        val dns = byteArrayOf(
            0x00, 0x01, 0x01, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x03, 'a'.code.toByte(), 'd'.code.toByte(), 's'.code.toByte(),
            0xC0.toByte(), 0x16,
            0x00, 0x01,
            0x00, 0x01,
            0x07, 'e'.code.toByte(), 'x'.code.toByte(), 'a'.code.toByte(),
            'm'.code.toByte(), 'p'.code.toByte(), 'l'.code.toByte(), 'e'.code.toByte(),
            0x03, 'c'.code.toByte(), 'o'.code.toByte(), 'm'.code.toByte(),
            0x00,
        )
        val (domain, _) = DnsPacketParser.parseDomainName(dns, 12)!!
        assertEquals("ads.example.com", domain)
    }

    @Test
    fun `handles chained compression pointers`() {
        val dns = byteArrayOf(
            0x00, 0x01, 0x01, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x03, 'f'.code.toByte(), 'o'.code.toByte(), 'o'.code.toByte(),
            0xC0.toByte(), 0x12,
            0x03, 'b'.code.toByte(), 'a'.code.toByte(), 'r'.code.toByte(),
            0xC0.toByte(), 0x18,
            0x03, 'c'.code.toByte(), 'o'.code.toByte(), 'm'.code.toByte(),
            0x00,
        )
        val (domain, _) = DnsPacketParser.parseDomainName(dns, 12)!!
        assertEquals("foo.bar.com", domain)
    }

    @Test
    fun `returns null for circular compression pointer`() {
        val dns = ByteArray(14)
        dns[0] = 0x00; dns[1] = 0x01
        dns[2] = 0x01; dns[3] = 0x00
        dns[4] = 0x00; dns[5] = 0x01
        dns[6] = 0x00; dns[7] = 0x00
        dns[8] = 0x00; dns[9] = 0x00
        dns[10] = 0x00; dns[11] = 0x00
        dns[12] = 0xC0.toByte()
        dns[13] = 0x0C
        assertNull(DnsPacketParser.parseDomainName(dns, 12))
    }

    @Test
    fun `returns null for pointer beyond message bounds`() {
        val dns = byteArrayOf(
            0x00, 0x01, 0x01, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0xC0.toByte(), 0xFF.toByte(),
        )
        assertNull(DnsPacketParser.parseDomainName(dns, 12))
    }

    @Test
    fun `returns null for label length exceeding 63`() {
        val dns = ByteArray(20)
        dns[12] = 0x40
        assertNull(DnsPacketParser.parseDomainName(dns, 12))
    }

    @Test
    fun `returns null for truncated label content`() {
        val dns = byteArrayOf(
            0x00, 0x01, 0x01, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x0A,
            'a'.code.toByte(), 'b'.code.toByte(),
        )
        assertNull(DnsPacketParser.parseDomainName(dns, 12))
    }

    @Test
    fun `domain name ending position is correct for simple name`() {
        val dns = byteArrayOf(
            0x00, 0x01, 0x01, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x03, 'c'.code.toByte(), 'o'.code.toByte(), 'm'.code.toByte(),
            0x00,
            0x00, 0x01,
            0x00, 0x01,
        )
        val (domain, endPos) = DnsPacketParser.parseDomainName(dns, 12)!!
        assertEquals("com", domain)
        assertEquals(17, endPos)
        assertEquals(1, DnsPacketParser.readUShort(dns, endPos))
    }
}
