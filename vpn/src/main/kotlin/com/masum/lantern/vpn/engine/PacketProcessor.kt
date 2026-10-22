package com.masum.lantern.vpn.engine

import com.masum.lantern.domain.model.DnsQuery
import com.masum.lantern.domain.repository.BlocklistRepository
import com.masum.lantern.vpn.attribution.ConnectionOwnerResolver
import com.masum.lantern.vpn.parser.DnsPacketParser
import com.masum.lantern.vpn.session.MonitoringSessionBus

class PacketProcessor(
    private val blocklistRepository: BlocklistRepository,
    private val sessionBus: MonitoringSessionBus,
    private val forwarder: DnsForwarder,
    private val resolver: ConnectionOwnerResolver,
    private val sessionId: Long,
) {
    fun process(rawPacket: ByteArray) {
        val parsed = DnsPacketParser.parse(rawPacket) ?: return
        val rawDns = DnsPacketParser.extractDnsPayload(rawPacket) ?: return

        val verdict = blocklistRepository.classify(parsed.domain)
        val uid = resolver.getUid(parsed)
        val appInfo = resolver.resolveAppInfo(uid)

        val query = DnsQuery(
            sessionId = sessionId,
            domain = parsed.domain,
            timestamp = System.currentTimeMillis(),
            verdict = verdict,
            appInfo = appInfo,
        )

        sessionBus.tryEmit(query)
        forwarder.forward(parsed, rawDns)
    }
}
