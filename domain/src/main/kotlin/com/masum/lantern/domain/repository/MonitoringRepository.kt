package com.masum.lantern.domain.repository

import com.masum.lantern.domain.model.MonitoringState
import kotlinx.coroutines.flow.Flow

interface MonitoringRepository {
    suspend fun startMonitoring(): Result<Unit>
    suspend fun stopMonitoring()
    suspend fun pauseMonitoring(durationMs: Long)
    suspend fun resumeMonitoring()
    fun observeMonitoringState(): Flow<MonitoringState>
}
