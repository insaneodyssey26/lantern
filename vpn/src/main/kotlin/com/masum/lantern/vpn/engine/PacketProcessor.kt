package com.masum.lantern.vpn.engine

import com.masum.lantern.domain.model.DnsQuery
import com.masum.lantern.domain.model.Verdict
import com.masum.lantern.domain.repository.BlocklistRepository
import com.masum.lantern.domain.repository.SettingsRepository
import com.masum.lantern.vpn.attribution.ConnectionOwnerResolver
import com.masum.lantern.vpn.parser.DnsPacketBuilder
import com.masum.lantern.vpn.parser.DnsPacketParser
import com.masum.lantern.vpn.session.MonitoringSessionBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PacketProcessor(
    private val blocklistRepository: BlocklistRepository,
    private val sessionBus: MonitoringSessionBus,
    private val forwarder: DnsForwarder,
    private val resolver: ConnectionOwnerResolver,
    private val settingsRepository: SettingsRepository,
    private val scope: CoroutineScope,
    private val sessionId: Long,
) {
    @Volatile private var isBlockingEnabled: Boolean = true
    @Volatile private var userAllowedDomains: Set<String> = emptySet()
    @Volatile private var userBlockedDomains: Set<String> = emptySet()

    init {
        scope.launch {
            settingsRepository.isBlockingEnabled().collect { enabled ->
                isBlockingEnabled = enabled
            }
        }
        scope.launch {
            settingsRepository.getUserAllowedDomains().collect { allowed ->
                userAllowedDomains = allowed
            }
        }
        scope.launch {
            settingsRepository.getUserBlockedDomains().collect { blocked ->
                userBlockedDomains = blocked
            }
        }
    }

    private fun isDomainOverriddenAllowed(domain: String): Boolean {
        if (userAllowedDomains.isEmpty()) return false
        val normalized = domain.trim().lowercase()
        if (userAllowedDomains.contains(normalized)) return true
        return userAllowedDomains.any { allowed ->
            normalized.endsWith(".$allowed")
        }
    }

    private fun isDomainOverriddenBlocked(domain: String): Boolean {
        if (userBlockedDomains.isEmpty()) return false
        val normalized = domain.trim().lowercase()
        if (userBlockedDomains.contains(normalized)) return true
        return userBlockedDomains.any { blocked ->
            normalized.endsWith(".$blocked")
        }
    }

    fun process(rawPacket: ByteArray) {
        val parsed = DnsPacketParser.parse(rawPacket) ?: return
        val rawDns = DnsPacketParser.extractDnsPayload(rawPacket) ?: return

        val domain = parsed.domain
        val isExplicitlyAllowed = isDomainOverriddenAllowed(domain)
        val isExplicitlyBlocked = !isExplicitlyAllowed && isDomainOverriddenBlocked(domain)

        val verdict = when {
            isExplicitlyAllowed -> Verdict.Clean
            isExplicitlyBlocked -> Verdict.Tracker(category = "Custom Rule")
            else -> blocklistRepository.classify(domain)
        }
        val uid = resolver.getUid(parsed)
        val appInfo = resolver.resolveAppInfo(uid)

        val isBlockedNow = (isExplicitlyBlocked || (verdict is Verdict.Tracker && isBlockingEnabled)) && !isExplicitlyAllowed

        val query = DnsQuery(
            sessionId = sessionId,
            domain = domain,
            timestamp = System.currentTimeMillis(),
            verdict = verdict,
            appInfo = appInfo,
            isBlocked = isBlockedNow,
        )

        sessionBus.tryEmit(query)

        if (isBlockedNow) {
            val sinkPacket = DnsPacketBuilder.buildNxDomainResponse(
                rawDnsQuery = rawDns,
                transactionId = parsed.transactionId,
                sourceIp = parsed.destIp,
                destIp = parsed.sourceIp,
                sourcePort = 53,
                destPort = parsed.sourcePort
            )
            forwarder.writePacket(sinkPacket)
        } else {
            forwarder.forward(parsed, rawDns)
        }
    }
}
