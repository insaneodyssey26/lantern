package com.masum.lantern.vpn.parser

object DnsPacketBuilder {

    fun wrapResponse(
        dnsResponse: ByteArray,
        sourceIp: Int,
        destIp: Int,
        sourcePort: Int,
        destPort: Int,
    ): ByteArray {
        val udpLen = 8 + dnsResponse.size
        val totalLen = 20 + udpLen
        val packet = ByteArray(totalLen)

        packet[0] = 0x45.toByte()
        packet[1] = 0x00
        writeUShort(packet, 2, totalLen)
        writeUShort(packet, 4, 0x0000)
        packet[6] = 0x40
        packet[7] = 0x00
        packet[8] = 0x40
        packet[9] = 0x11
        packet[10] = 0x00
        packet[11] = 0x00
        writeInt(packet, 12, sourceIp)
        writeInt(packet, 16, destIp)

        val checksum = ipChecksum(packet, 0, 20)
        writeUShort(packet, 10, checksum)

        val udpOff = 20
        writeUShort(packet, udpOff, sourcePort)
        writeUShort(packet, udpOff + 2, destPort)
        writeUShort(packet, udpOff + 4, udpLen)
        writeUShort(packet, udpOff + 6, 0x0000)

        dnsResponse.copyInto(packet, udpOff + 8)
        return packet
    }

    private fun ipChecksum(data: ByteArray, offset: Int, length: Int): Int {
        var sum = 0
        var i = offset
        while (i < offset + length) {
            val hi = data[i].toInt() and 0xFF
            val lo = if (i + 1 < offset + length) data[i + 1].toInt() and 0xFF else 0
            sum += (hi shl 8) or lo
            i += 2
        }
        while (sum ushr 16 != 0) sum = (sum and 0xFFFF) + (sum ushr 16)
        return sum.inv() and 0xFFFF
    }

    private fun writeUShort(data: ByteArray, offset: Int, value: Int) {
        data[offset] = (value ushr 8).toByte()
        data[offset + 1] = (value and 0xFF).toByte()
    }

    private fun writeInt(data: ByteArray, offset: Int, value: Int) {
        data[offset] = (value ushr 24).toByte()
        data[offset + 1] = (value ushr 16 and 0xFF).toByte()
        data[offset + 2] = (value ushr 8 and 0xFF).toByte()
        data[offset + 3] = (value and 0xFF).toByte()
    }
}
