package com.masum.lantern.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import com.masum.lantern.domain.model.Verdict
import com.masum.lantern.domain.repository.BlocklistRepository
import com.masum.lantern.domain.repository.SettingsRepository
import com.masum.lantern.vpn.attribution.ConnectionOwnerResolver
import com.masum.lantern.vpn.engine.DnsForwarder
import com.masum.lantern.vpn.engine.PacketProcessor
import com.masum.lantern.vpn.engine.PacketReadLoop
import com.masum.lantern.vpn.session.MonitoringSessionBus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@AndroidEntryPoint
class LanternVpnService : VpnService() {

    @Inject lateinit var sessionBus: MonitoringSessionBus
    @Inject lateinit var blocklistRepository: BlocklistRepository
    @Inject lateinit var settingsRepository: SettingsRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val queryCount = AtomicInteger(0)
    private val blockedCount = AtomicInteger(0)
    @Volatile private var isBlocking = true
    @Volatile private var activeProtocol = "UDP"

    private var pfd: ParcelFileDescriptor? = null
    private var readLoop: PacketReadLoop? = null
    private var forwarder: DnsForwarder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val sessionId = intent.getLongExtra(EXTRA_SESSION_ID, System.currentTimeMillis())
                startTunnel(sessionId)
            }
            ACTION_STOP -> stopTunnel()
            ACTION_TOGGLE_SHIELD -> {
                scope.launch {
                    val current = isBlocking
                    settingsRepository.setBlockingEnabled(!current)
                    updateNotificationImmediately()
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun startTunnel(sessionId: Long) {
        sessionBus.resetReplayCache()
        queryCount.set(0)
        blockedCount.set(0)
        ensureNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        val builder = Builder()
            .addAddress(TUN_ADDRESS, 32)
            .addDnsServer(SENTINEL_DNS)
            .addRoute(SENTINEL_DNS, 32)
            .setMtu(1500)
            .setBlocking(true)
            .setSession(SESSION_NAME)

        runCatching { builder.addDisallowedApplication(packageName) }

        val bypassedApps = runCatching {
            runBlocking { settingsRepository.getBypassedApps().first() }
        }.getOrDefault(emptySet())

        bypassedApps.forEach { appPkg ->
            runCatching { builder.addDisallowedApplication(appPkg) }
        }

        val descriptor = builder.establish() ?: run { stopSelf(); return }

        pfd = descriptor

        val forwarderInstance = DnsForwarder(this, descriptor, scope, settingsRepository)
        forwarder = forwarderInstance
        val resolver = ConnectionOwnerResolver(this)
        val processor = PacketProcessor(
            blocklistRepository = blocklistRepository,
            sessionBus = sessionBus,
            forwarder = forwarderInstance,
            resolver = resolver,
            settingsRepository = settingsRepository,
            scope = scope,
            sessionId = sessionId
        )

        readLoop = PacketReadLoop(descriptor) { packet ->
            processor.process(packet)
        }.also { it.start() }

        scope.launch { observeSettings() }
        scope.launch { countQueries() }
        scope.launch { refreshNotificationPeriodically() }
    }

    private suspend fun observeSettings() {
        scope.launch {
            settingsRepository.isBlockingEnabled().collect { enabled ->
                isBlocking = enabled
                updateNotificationImmediately()
            }
        }
        scope.launch {
            settingsRepository.getDnsProtocol().collect { proto ->
                activeProtocol = proto
                updateNotificationImmediately()
            }
        }
    }

    private suspend fun countQueries() {
        sessionBus.queries.collect { query ->
            queryCount.incrementAndGet()
            if (query.isBlocked || query.verdict is Verdict.Tracker) {
                blockedCount.incrementAndGet()
            }
        }
    }

    private suspend fun refreshNotificationPeriodically() {
        while (true) {
            delay(NOTIFICATION_REFRESH_MS)
            updateNotificationImmediately()
        }
    }

    private fun updateNotificationImmediately() {
        runCatching {
            getSystemService(NotificationManager::class.java)
                .notify(NOTIFICATION_ID, buildNotification())
        }
    }

    private fun stopTunnel() {
        readLoop?.stop()
        readLoop = null
        forwarder?.close()
        forwarder = null
        pfd?.close()
        pfd = null
        queryCount.set(0)
        blockedCount.set(0)
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
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Lantern Protection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active Lantern DNS protection status and real-time query metrics."
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val total = queryCount.get()
        val blocked = blockedCount.get()
        val percentage = if (total > 0) ((blocked.toFloat() / total) * 100).toInt() else 0

        val title = if (isBlocking) "Lantern Active · Blocking" else "Lantern Active · Audit Only"
        val contentText = if (total == 0) {
            "Listening for network requests ($activeProtocol)"
        } else {
            "$total queries · $blocked blocked ($percentage%) · $activeProtocol"
        }

        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = launchIntent?.let {
            PendingIntent.getActivity(
                this,
                0,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val stopIntent = Intent("${packageName}.action.STOP_VPN").apply {
            setPackage(packageName)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val toggleShieldIntent = Intent("${packageName}.action.TOGGLE_SHIELD").apply {
            setPackage(packageName)
        }
        val toggleShieldPendingIntent = PendingIntent.getBroadcast(
            this,
            2,
            toggleShieldIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val toggleModeTitle = if (isBlocking) "Audit Only" else "Block Trackers"

        val builder = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_stat_lantern)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .addAction(
                Notification.Action.Builder(
                    null,
                    toggleModeTitle,
                    toggleShieldPendingIntent
                ).build()
            )
            .addAction(
                Notification.Action.Builder(
                    null,
                    "Stop",
                    stopPendingIntent
                ).build()
            )

        if (contentPendingIntent != null) {
            builder.setContentIntent(contentPendingIntent)
        }

        return builder.build()
    }

    companion object {
        const val ACTION_START = "com.masum.lantern.vpn.START"
        const val ACTION_STOP = "com.masum.lantern.vpn.STOP"
        const val ACTION_TOGGLE_SHIELD = "com.masum.lantern.vpn.TOGGLE_SHIELD"
        const val EXTRA_SESSION_ID = "session_id"

        private const val TUN_ADDRESS = "10.0.0.1"
        private const val SENTINEL_DNS = "10.0.0.2"
        private const val SESSION_NAME = "Lantern"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "lantern_protection"
        private const val NOTIFICATION_REFRESH_MS = 3_000L
    }
}
