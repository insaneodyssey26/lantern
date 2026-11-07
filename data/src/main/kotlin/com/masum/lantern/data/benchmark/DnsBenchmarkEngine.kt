package com.masum.lantern.data.benchmark

import com.masum.lantern.core.common.config.ResolverConfig
import com.masum.lantern.domain.model.BenchmarkStatus
import com.masum.lantern.domain.model.DnsBenchmarkItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Dns
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.Collections
import java.util.concurrent.TimeUnit
import javax.net.ssl.SNIHostName
import javax.net.ssl.SSLParameters
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import kotlin.math.abs

class DnsBenchmarkEngine {

    private val mediaType = "application/dns-message".toMediaType()
    private val sslSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(2_000, TimeUnit.MILLISECONDS)
        .readTimeout(2_000, TimeUnit.MILLISECONDS)
        .writeTimeout(2_000, TimeUnit.MILLISECONDS)
        .dns(object : Dns {
            override fun lookup(hostname: String): List<InetAddress> = when (hostname) {
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
                "dns.adguard-dns.com" -> listOf(
                    InetAddress.getByAddress(hostname, byteArrayOf(94.toByte(), 140.toByte(), 14.toByte(), 14.toByte())),
                    InetAddress.getByAddress(hostname, byteArrayOf(94.toByte(), 140.toByte(), 15.toByte(), 15.toByte()))
                )
                else -> Dns.SYSTEM.lookup(hostname)
            }
        })
        .build()

    suspend fun probeServer(
        providerId: String,
        name: String,
        ip: String,
        protocol: String,
        pings: Int = 3
    ): DnsBenchmarkItem = withContext(Dispatchers.IO) {
        val latencies = mutableListOf<Long>()

        for (i in 0 until pings) {
            val rtt = probeOnce(ip, protocol)
            if (rtt != null && rtt >= 0) {
                latencies.add(rtt)
            }
        }

        if (latencies.isEmpty()) {
            DnsBenchmarkItem(
                providerId = providerId,
                name = name,
                ip = ip,
                status = BenchmarkStatus.FAILED,
                latencyMs = null,
                minLatencyMs = null,
                maxLatencyMs = null,
                jitterMs = null,
                successPings = 0,
                totalPings = pings,
                isFastest = false
            )
        } else {
            val avgLatency = latencies.average().toLong()
            val minLatency = latencies.minOrNull() ?: avgLatency
            val maxLatency = latencies.maxOrNull() ?: avgLatency
            val jitter = if (latencies.size > 1) {
                var diffSum = 0L
                for (j in 0 until latencies.size - 1) {
                    diffSum += abs(latencies[j + 1] - latencies[j])
                }
                diffSum / (latencies.size - 1)
            } else 0L

            DnsBenchmarkItem(
                providerId = providerId,
                name = name,
                ip = ip,
                status = BenchmarkStatus.SUCCESS,
                latencyMs = avgLatency,
                minLatencyMs = minLatency,
                maxLatencyMs = maxLatency,
                jitterMs = jitter,
                successPings = latencies.size,
                totalPings = pings,
                isFastest = false
            )
        }
    }

    private fun probeOnce(ip: String, protocol: String): Long? {
        val queryBytes = DnsQueryWireFormat.buildStandardAQuery("google.com")
        val startNs = System.nanoTime()

        val success = when (protocol) {
            "DOH" -> probeDoh(ip, queryBytes)
            "DOT" -> probeDot(ip, queryBytes)
            else -> probeUdp(ip, queryBytes)
        }

        val elapsedMs = (System.nanoTime() - startNs) / 1_000_000L
        return if (success) elapsedMs else null
    }

    private fun probeUdp(ip: String, query: ByteArray): Boolean = runCatching {
        DatagramSocket().use { socket ->
            socket.soTimeout = 2_000
            val dest = InetSocketAddress(ip, ResolverConfig.DNS_PORT)
            val packet = DatagramPacket(query, query.size, dest)
            socket.send(packet)

            val recvBuf = ByteArray(512)
            val recvPacket = DatagramPacket(recvBuf, recvBuf.size)
            socket.receive(recvPacket)
            recvPacket.length >= 12
        }
    }.getOrDefault(false)

    private fun probeDoh(ip: String, query: ByteArray): Boolean = runCatching {
        val url = ResolverConfig.getDohUrl(ip)
        val request = Request.Builder()
            .url(url)
            .post(query.toRequestBody(mediaType))
            .header("Accept", "application/dns-message")
            .header("User-Agent", "Lantern-Android-Benchmark")
            .build()

        httpClient.newCall(request).execute().use { response ->
            response.isSuccessful && (response.body.bytes().size) >= 12
        }
    }.getOrDefault(false)

    private fun probeDot(ip: String, query: ByteArray): Boolean = runCatching {
        val sniHost = ResolverConfig.getSniHostname(ip)
        val rawSocket = Socket()
        rawSocket.soTimeout = 2_000
        rawSocket.connect(InetSocketAddress(ip, ResolverConfig.DOT_PORT), 2_000)

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

        outStream.writeShort(query.size)
        outStream.write(query)
        outStream.flush()

        val responseLength = inStream.readUnsignedShort()
        val responseBytes = ByteArray(responseLength)
        inStream.readFully(responseBytes)

        sslSocket.close()
        responseBytes.size >= 12
    }.getOrDefault(false)
}
