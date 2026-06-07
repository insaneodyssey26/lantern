package com.masum.vigil.vpn.engine

import com.masum.vigil.domain.model.DnsQuery
import com.masum.vigil.domain.repository.BlocklistRepository
import com.masum.vigil.vpn.attribution.ConnectionOwnerResolver
import com.masum.vigil.vpn.parser.DnsPacketParser
import com.masum.vigil.vpn.session.MonitoringSessionBus

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
