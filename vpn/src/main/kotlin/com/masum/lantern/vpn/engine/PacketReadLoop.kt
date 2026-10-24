package com.masum.lantern.vpn.engine

import android.os.ParcelFileDescriptor
import java.io.FileInputStream
import java.io.IOException

class PacketReadLoop(
    private val pfd: ParcelFileDescriptor,
    private val onPacket: (ByteArray) -> Unit,
) {
    private val thread = Thread(::loop, "lantern-packet-reader").apply { isDaemon = true }

    fun start() = thread.start()

    fun stop() {
        thread.interrupt()
    }

    private fun loop() {
        val input = FileInputStream(pfd.fileDescriptor)
        val buffer = ByteArray(32_767)
        try {
            while (!Thread.currentThread().isInterrupted) {
                val n = input.read(buffer)
                if (n > 0) onPacket(buffer.copyOf(n))
            }
        } catch (_: IOException) {
        } catch (_: InterruptedException) {
        }
    }
}
