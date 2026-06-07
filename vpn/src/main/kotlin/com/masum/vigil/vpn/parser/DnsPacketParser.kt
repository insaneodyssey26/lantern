package com.masum.vigil.vpn.parser

object DnsPacketParser {

    private const val PROTO_UDP = 17
    private const val DNS_PORT = 53
    private const val MAX_LABEL_LENGTH = 63
    private const val MAX_POINTER_JUMPS = 10
    private const val DNS_HEADER_SIZE = 12

    fun parse(rawPacket: ByteArray): ParsedDnsQuery? = runCatching {
        parseInternal(rawPacket)
    }.getOrNull()

    fun extractDnsPayload(rawPacket: ByteArray): ByteArray? {
        if (rawPacket.size < 28) return null
        val ipHeaderLen = (rawPacket[0].toInt() and 0x0F) * 4
        val udpOffset = ipHeaderLen
        if (udpOffset + 8 > rawPacket.size) return null
        val udpLen = readUShort(rawPacket, udpOffset + 4)
        val dnsOffset = udpOffset + 8
        val dnsLen = udpLen - 8
        if (dnsLen < 1 || dnsOffset + dnsLen > rawPacket.size) return null
        return rawPacket.copyOfRange(dnsOffset, dnsOffset + dnsLen)
    }

    private fun parseInternal(rawPacket: ByteArray): ParsedDnsQuery? {
        if (rawPacket.size < 28) return null

        val versionIhl = rawPacket[0].toInt() and 0xFF
        if (versionIhl ushr 4 != 4) return null
        val ipHeaderLen = (versionIhl and 0x0F) * 4
        if (ipHeaderLen < 20 || rawPacket.size < ipHeaderLen + 8) return null

        if (rawPacket[9].toInt() and 0xFF != PROTO_UDP) return null

        val sourceIp = readInt(rawPacket, 12)
        val destIp = readInt(rawPacket, 16)

        val udpOffset = ipHeaderLen
        val sourcePort = readUShort(rawPacket, udpOffset)
        val destPort = readUShort(rawPacket, udpOffset + 2)
        if (destPort != DNS_PORT) return null

        val udpLen = readUShort(rawPacket, udpOffset + 4)
        val dnsOffset = udpOffset + 8
        val dnsLen = udpLen - 8
        if (dnsLen < DNS_HEADER_SIZE || dnsOffset + dnsLen > rawPacket.size) return null

        val dns = rawPacket.copyOfRange(dnsOffset, dnsOffset + dnsLen)

        val flags = readUShort(dns, 2)
        if (flags and 0x8000 != 0) return null

        val qdCount = readUShort(dns, 4)
        if (qdCount == 0) return null

        val txId = readUShort(dns, 0)
        val (domain, endPos) = parseDomainName(dns, DNS_HEADER_SIZE) ?: return null
        if (domain.isEmpty()) return null
        if (endPos + 4 > dns.size) return null

        val queryType = readUShort(dns, endPos)

        return ParsedDnsQuery(
            transactionId = txId,
            domain = domain,
            queryType = queryType,
            sourceIp = sourceIp,
            destIp = destIp,
            sourcePort = sourcePort,
        )
    }

    internal fun parseDomainName(dns: ByteArray, startPos: Int): Pair<String, Int>? {
        val labels = mutableListOf<String>()
        var pos = startPos
        var endPos = -1
        var jumps = 0

        while (pos < dns.size) {
            val length = dns[pos].toInt() and 0xFF
            when {
                length == 0 -> {
                    val nameEnd = if (endPos == -1) pos + 1 else endPos
                    return labels.joinToString(".") to nameEnd
                }
                length and 0xC0 == 0xC0 -> {
                    if (pos + 1 >= dns.size) return null
                    if (++jumps > MAX_POINTER_JUMPS) return null
                    if (endPos == -1) endPos = pos + 2
                    val ptrOffset = ((length and 0x3F) shl 8) or (dns[pos + 1].toInt() and 0xFF)
                    if (ptrOffset >= dns.size) return null
                    pos = ptrOffset
                }
                length > MAX_LABEL_LENGTH -> return null
                else -> {
                    val labelEnd = pos + 1 + length
                    if (labelEnd > dns.size) return null
                    labels.add(String(dns, pos + 1, length, Charsets.US_ASCII))
                    pos = labelEnd
                }
            }
        }
        return null
    }

    internal fun readUShort(data: ByteArray, offset: Int): Int =
        ((data[offset].toInt() and 0xFF) shl 8) or (data[offset + 1].toInt() and 0xFF)

    private fun readInt(data: ByteArray, offset: Int): Int =
        ((data[offset].toInt() and 0xFF) shl 24) or
        ((data[offset + 1].toInt() and 0xFF) shl 16) or
        ((data[offset + 2].toInt() and 0xFF) shl 8) or
        (data[offset + 3].toInt() and 0xFF)
}
