package com.masum.vigil.domain.repository

import com.masum.vigil.domain.model.MonitoringState
import kotlinx.coroutines.flow.Flow

interface MonitoringRepository {
    suspend fun startMonitoring(): Result<Unit>
    suspend fun stopMonitoring()
    fun observeMonitoringState(): Flow<MonitoringState>
}
