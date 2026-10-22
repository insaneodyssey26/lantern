package com.masum.lantern.vpn.engine

import android.net.VpnService
import android.os.ParcelFileDescriptor
import com.masum.lantern.core.common.config.ResolverConfig
import com.masum.lantern.domain.repository.SettingsRepository
import com.masum.lantern.vpn.cache.DnsResponseCache
import com.masum.lantern.vpn.parser.DnsPacketBuilder
import com.masum.lantern.vpn.parser.ParsedDnsQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.ArrayBlockingQueue

class DnsForwarder(
    private val service: VpnService,
    pfd: ParcelFileDescriptor,
    private val scope: CoroutineScope,
    private val settingsRepository: SettingsRepository,
    private val cache: DnsResponseCache = DnsResponseCache(maxEntries = 2048),
) {
    private val output = FileOutputStream(pfd.fileDescriptor)
    @Volatile private var upstreamDns: String = ResolverConfig.PRIMARY_DNS

    private val socketPool = ArrayBlockingQueue<DatagramSocket>(POOL_SIZE)

    init {
        scope.launch {
            settingsRepository.getUpstreamDns().collect { dns ->
                upstreamDns = dns
            }
        }
        repeat(POOL_SIZE) {
            runCatching {
                val socket = DatagramSocket().apply {
                    service.protect(this)
                    soTimeout = ResolverConfig.QUERY_TIMEOUT_MS.toInt()
                }
                socketPool.offer(socket)
            }
        }
    }

    fun writePacket(rawPacket: ByteArray) {
        synchronized(output) {
            runCatching { output.write(rawPacket) }
        }
    }

    fun forward(query: ParsedDnsQuery, rawDns: ByteArray) {
        val cachedPayload = cache.get(query.domain, query.queryType, query.transactionId)
        if (cachedPayload != null) {
            val wrapped = DnsPacketBuilder.wrapResponse(
                dnsResponse = cachedPayload,
                sourceIp = query.destIp,
                destIp = query.sourceIp,
                sourcePort = ResolverConfig.DNS_PORT,
                destPort = query.sourcePort,
            )
            writePacket(wrapped)
            return
        }

        scope.launch(Dispatchers.IO) {
            runCatching { sendAndReply(query, rawDns) }
        }
    }

    private fun sendAndReply(query: ParsedDnsQuery, rawDns: ByteArray) {
        var socket = socketPool.poll()
        var fromPool = true

        if (socket == null || socket.isClosed) {
            socket = DatagramSocket().apply {
                service.protect(this)
                soTimeout = ResolverConfig.QUERY_TIMEOUT_MS.toInt()
            }
            fromPool = false
        }

        try {
            val upstream = InetAddress.getByName(upstreamDns)
            socket.send(DatagramPacket(rawDns, rawDns.size, upstream, ResolverConfig.DNS_PORT))

            val buf = ByteArray(4096)
            val resp = DatagramPacket(buf, buf.size)
            socket.receive(resp)

            val dnsPayload = buf.copyOf(resp.length)
            cache.put(query.domain, query.queryType, dnsPayload)

            val wrapped = DnsPacketBuilder.wrapResponse(
                dnsResponse = dnsPayload,
                sourceIp = query.destIp,
                destIp = query.sourceIp,
                sourcePort = ResolverConfig.DNS_PORT,
                destPort = query.sourcePort,
            )
            writePacket(wrapped)

            if (fromPool && !socket.isClosed) {
                if (!socketPool.offer(socket)) {
                    socket.close()
                }
            } else if (!fromPool) {
                if (!socketPool.offer(socket)) {
                    socket.close()
                }
            }
        } catch (e: Exception) {
            socket.close()
        }
    }

    fun close() {
        cache.clear()
        while (true) {
            val s = socketPool.poll() ?: break
            runCatching { s.close() }
        }
    }

    companion object {
        private const val POOL_SIZE = 4
    }
}
