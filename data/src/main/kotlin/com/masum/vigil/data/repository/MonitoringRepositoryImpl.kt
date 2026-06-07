package com.masum.vigil.data.repository

import android.content.Context
import android.content.Intent
import android.net.VpnService
import com.masum.vigil.core.common.dispatcher.DispatcherProvider
import com.masum.vigil.core.database.dao.SessionDao
import com.masum.vigil.core.database.entity.SessionEntity
import com.masum.vigil.domain.model.MonitoringState
import com.masum.vigil.domain.repository.MonitoringRepository
import com.masum.vigil.vpn.VigilVpnService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MonitoringRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionDao: SessionDao,
    private val dispatchers: DispatcherProvider,
) : MonitoringRepository {

    private val _state = MutableStateFlow<MonitoringState>(MonitoringState.Idle)
    private var currentSessionId: Long? = null

    override suspend fun startMonitoring(): Result<Unit> = withContext(dispatchers.io) {
        runCatching {
            val prepareIntent = VpnService.prepare(context)
            if (prepareIntent != null) error("VPN consent required")

            val now = System.currentTimeMillis()
            val sessionId = sessionDao.insert(SessionEntity(startedAt = now, endedAt = null))
            currentSessionId = sessionId

            context.startForegroundService(
                Intent(context, VigilVpnService::class.java).apply {
                    action = VigilVpnService.ACTION_START
                    putExtra(VigilVpnService.EXTRA_SESSION_ID, sessionId)
                }
            )

            _state.value = MonitoringState.Active(sessionId = sessionId, startedAt = now)
        }
    }

    override suspend fun stopMonitoring(): Unit = withContext(dispatchers.io) {
        currentSessionId?.let { id ->
            sessionDao.updateEndTime(id, System.currentTimeMillis())
        }
        currentSessionId = null

        context.startService(
            Intent(context, VigilVpnService::class.java).apply {
                action = VigilVpnService.ACTION_STOP
            }
        )

        _state.value = MonitoringState.Idle
    }

    override fun observeMonitoringState(): Flow<MonitoringState> = _state.asStateFlow()
}
