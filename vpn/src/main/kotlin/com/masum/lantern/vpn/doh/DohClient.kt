package com.masum.lantern.vpn.doh

import android.net.VpnService
import com.masum.lantern.core.common.config.ResolverConfig
import okhttp3.ConnectionPool
import okhttp3.Dns
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit
import javax.net.SocketFactory

class DohClient(
    private val vpnService: VpnService,
) {
    private val mediaType = "application/dns-message".toMediaType()

    private val protectedSocketFactory = object : SocketFactory() {
        override fun createSocket(): Socket {
            val socket = Socket()
            socket.bind(InetSocketAddress(0))
            vpnService.protect(socket)
            return socket
        }

        override fun createSocket(host: String?, port: Int): Socket {
            val socket = createSocket()
            socket.connect(InetSocketAddress(host, port))
            return socket
        }

        override fun createSocket(host: String?, port: Int, localHost: InetAddress?, localPort: Int): Socket {
            val socket = createSocket()
            socket.connect(InetSocketAddress(host, port))
            return socket
        }

        override fun createSocket(address: InetAddress?, port: Int): Socket {
            val socket = createSocket()
            socket.connect(InetSocketAddress(address, port))
            return socket
        }

        override fun createSocket(address: InetAddress?, port: Int, localAddress: InetAddress?, localPort: Int): Socket {
            val socket = createSocket()
            socket.connect(InetSocketAddress(address, port))
            return socket
        }
    }

    private val bootstrapDns = object : Dns {
        override fun lookup(hostname: String): List<InetAddress> {
            return when (hostname) {
                "cloudflare-dns.com", "one.one.one.one" -> listOf(
                    InetAddress.getByAddress(hostname, byteArrayOf(1, 1, 1, 1)),
                    InetAddress.getByAddress(hostname, byteArrayOf(1, 0, 0, 1))
                )
                "dns.quad9.net" -> listOf(
                    InetAddress.getByAddress(hostname, byteArrayOf(9, 9, 9, 9)),
                    InetAddress.getByAddress(hostname, byteArrayOf(149.toByte(), 112.toByte(), 112.toByte(), 112.toByte()))
                )
                "dns.google" -> listOf(
                    InetAddress.getByAddress(hostname, byteArrayOf(8, 8, 8, 8)),
                    InetAddress.getByAddress(hostname, byteArrayOf(8, 8, 4, 4))
                )
                else -> Dns.SYSTEM.lookup(hostname)
            }
        }
    }

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .socketFactory(protectedSocketFactory)
        .dns(bootstrapDns)
        .connectTimeout(ResolverConfig.QUERY_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        .readTimeout(ResolverConfig.QUERY_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        .writeTimeout(ResolverConfig.QUERY_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        .connectionPool(ConnectionPool(8, 60, TimeUnit.SECONDS))
        .retryOnConnectionFailure(true)
        .build()

    fun query(rawDnsQuery: ByteArray, upstreamDns: String): ByteArray? {
        val endpointUrl = ResolverConfig.getDohUrl(upstreamDns)
        val requestBody = rawDnsQuery.toRequestBody(mediaType)
        val request = Request.Builder()
            .url(endpointUrl)
            .post(requestBody)
            .header("Accept", "application/dns-message")
            .header("User-Agent", "Vigil-Android-DoH")
            .build()

        return runCatching {
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body.bytes()
                } else {
                    null
                }
            }
        }.getOrNull()
    }

    fun close() {
        runCatching {
            httpClient.dispatcher.executorService.shutdown()
            httpClient.connectionPool.evictAll()
        }
    }
}
