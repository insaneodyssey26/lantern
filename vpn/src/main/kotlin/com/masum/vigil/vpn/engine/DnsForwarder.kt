package com.masum.vigil.vpn.engine

import android.net.VpnService
import android.os.ParcelFileDescriptor
import com.masum.vigil.core.common.config.ResolverConfig
import com.masum.vigil.domain.repository.SettingsRepository
import com.masum.vigil.vpn.parser.DnsPacketBuilder
import com.masum.vigil.vpn.parser.ParsedDnsQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class DnsForwarder(
    private val service: VpnService,
    pfd: ParcelFileDescriptor,
    private val scope: CoroutineScope,
    private val settingsRepository: SettingsRepository,
) {
    private val output = FileOutputStream(pfd.fileDescriptor)
    @Volatile private var upstreamDns: String = ResolverConfig.PRIMARY_DNS

    init {
        scope.launch {
            settingsRepository.getUpstreamDns().collect { dns ->
                upstreamDns = dns
            }
        }
    }

    fun forward(query: ParsedDnsQuery, rawDns: ByteArray) {
        scope.launch(Dispatchers.IO) {
            runCatching { sendAndReply(query, rawDns) }
        }
    }

    private fun sendAndReply(query: ParsedDnsQuery, rawDns: ByteArray) {
        DatagramSocket().use { socket ->
            service.protect(socket)
            socket.soTimeout = ResolverConfig.QUERY_TIMEOUT_MS.toInt()

            val upstream = InetAddress.getByName(upstreamDns)
            socket.send(DatagramPacket(rawDns, rawDns.size, upstream, ResolverConfig.DNS_PORT))

            val buf = ByteArray(4096)
            val resp = DatagramPacket(buf, buf.size)
            socket.receive(resp)

            val wrapped = DnsPacketBuilder.wrapResponse(
                dnsResponse = buf.copyOf(resp.length),
                sourceIp = query.destIp,
                destIp = query.sourceIp,
                sourcePort = ResolverConfig.DNS_PORT,
                destPort = query.sourcePort,
            )
            synchronized(output) { output.write(wrapped) }
        }
    }
}
