package com.masum.lantern.data.benchmark

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import kotlin.random.Random

object DnsQueryWireFormat {

    fun buildStandardAQuery(domain: String = "google.com"): ByteArray {
        val out = ByteArrayOutputStream()
        val data = DataOutputStream(out)

        val txId = Random.nextInt(0x0001, 0xFFFF)
        data.writeShort(txId)
        data.writeShort(0x0100)
        data.writeShort(1)
        data.writeShort(0)
        data.writeShort(0)
        data.writeShort(0)

        for (part in domain.split('.')) {
            val bytes = part.toByteArray(Charsets.US_ASCII)
            data.writeByte(bytes.size)
            data.write(bytes)
        }
        data.writeByte(0)

        data.writeShort(1)
        data.writeShort(1)

        return out.toByteArray()
    }
}
