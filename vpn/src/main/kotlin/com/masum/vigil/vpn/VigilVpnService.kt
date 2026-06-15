package com.masum.vigil.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import com.masum.vigil.domain.repository.BlocklistRepository
import com.masum.vigil.domain.repository.SettingsRepository
import com.masum.vigil.vpn.attribution.ConnectionOwnerResolver
import com.masum.vigil.vpn.engine.DnsForwarder
import com.masum.vigil.vpn.engine.PacketProcessor
import com.masum.vigil.vpn.engine.PacketReadLoop
import com.masum.vigil.vpn.session.MonitoringSessionBus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@AndroidEntryPoint
class VigilVpnService : VpnService() {

    @Inject lateinit var sessionBus: MonitoringSessionBus
    @Inject lateinit var blocklistRepository: BlocklistRepository
    @Inject lateinit var settingsRepository: SettingsRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val queryCount = AtomicInteger(0)

    private var pfd: ParcelFileDescriptor? = null
    private var readLoop: PacketReadLoop? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val sessionId = intent.getLongExtra(EXTRA_SESSION_ID, System.currentTimeMillis())
                startTunnel(sessionId)
            }
            ACTION_STOP -> stopTunnel()
        }
        return START_NOT_STICKY
    }

    private fun startTunnel(sessionId: Long) {
        sessionBus.resetReplayCache()
        ensureNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        val descriptor = Builder()
            .addAddress(TUN_ADDRESS, 32)
            .addDnsServer(SENTINEL_DNS)
            .addRoute(SENTINEL_DNS, 32)
            .setMtu(1500)
            .setBlocking(true)
            .setSession(SESSION_NAME)
            .establish() ?: run { stopSelf(); return }

        pfd = descriptor

        val forwarder = DnsForwarder(this, descriptor, scope, settingsRepository)
        val resolver = ConnectionOwnerResolver(this)
        val processor = PacketProcessor(blocklistRepository, sessionBus, forwarder, resolver, sessionId)

        readLoop = PacketReadLoop(descriptor) { packet ->
            processor.process(packet)
        }.also { it.start() }

        scope.launch { countQueries() }
        scope.launch { refreshNotificationPeriodically() }
    }

    private suspend fun countQueries() {
        sessionBus.queries.collect { queryCount.incrementAndGet() }
    }

    private suspend fun refreshNotificationPeriodically() {
        while (true) {
            delay(NOTIFICATION_REFRESH_MS)
            getSystemService(NotificationManager::class.java)
                .notify(NOTIFICATION_ID, buildNotification())
        }
    }

    private fun stopTunnel() {
        readLoop?.stop()
        readLoop = null
        pfd?.close()
        pfd = null
        queryCount.set(0)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onRevoke() {
        stopTunnel()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun ensureNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Vigil Monitoring", NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    private fun buildNotification(): Notification {
        val count = queryCount.get()
        val text = if (count == 0) "Monitoring DNS traffic" else "$count queries captured"

        val stopPendingIntent = PendingIntent.getService(
            this, 0,
            Intent(this, VigilVpnService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE,
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Vigil is active")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .addAction(
                Notification.Action.Builder(
                    null, "Stop", stopPendingIntent,
                ).build()
            )
            .build()
    }

    companion object {
        const val ACTION_START = "com.masum.vigil.vpn.START"
        const val ACTION_STOP = "com.masum.vigil.vpn.STOP"
        const val EXTRA_SESSION_ID = "session_id"

        private const val TUN_ADDRESS = "10.0.0.1"
        private const val SENTINEL_DNS = "10.0.0.2"
        private const val SESSION_NAME = "Vigil"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "vigil_monitoring"
        private const val NOTIFICATION_REFRESH_MS = 5_000L
    }
}
