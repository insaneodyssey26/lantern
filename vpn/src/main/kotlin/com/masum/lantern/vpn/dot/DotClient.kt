package com.masum.lantern.vpn.dot

import android.net.VpnService
import com.masum.lantern.core.common.config.ResolverConfig
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.Collections
import javax.net.ssl.SNIHostName
import javax.net.ssl.SSLParameters
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

class DotClient(
    private val vpnService: VpnService,
) {
    private val sslSocketFactory: SSLSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory

    fun query(rawDnsQuery: ByteArray, upstreamDns: String): ByteArray? {
        val sniHost = ResolverConfig.getSniHostname(upstreamDns)
        return runCatching {
            val rawSocket = Socket()
            rawSocket.bind(InetSocketAddress(0))
            vpnService.protect(rawSocket)
            rawSocket.soTimeout = ResolverConfig.QUERY_TIMEOUT_MS.toInt()
            rawSocket.connect(InetSocketAddress(upstreamDns, ResolverConfig.DOT_PORT), ResolverConfig.QUERY_TIMEOUT_MS.toInt())

            val sslSocket = sslSocketFactory.createSocket(
                rawSocket,
                sniHost,
                ResolverConfig.DOT_PORT,
                true
            ) as SSLSocket

            val sslParameters: SSLParameters = sslSocket.sslParameters
            sslParameters.serverNames = Collections.singletonList(SNIHostName(sniHost))
            sslParameters.endpointIdentificationAlgorithm = "HTTPS"
            sslSocket.sslParameters = sslParameters

            sslSocket.startHandshake()

            val outStream = DataOutputStream(sslSocket.outputStream)
            val inStream = DataInputStream(sslSocket.inputStream)

            outStream.writeShort(rawDnsQuery.size)
            outStream.write(rawDnsQuery)
            outStream.flush()

            val responseLength = inStream.readUnsignedShort()
            val responseBytes = ByteArray(responseLength)
            inStream.readFully(responseBytes)

            sslSocket.close()
            responseBytes
        }.getOrNull()
    }

    fun close() {
    }
}
