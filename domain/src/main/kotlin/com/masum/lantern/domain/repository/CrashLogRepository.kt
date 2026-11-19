package com.masum.lantern.domain.repository

import com.masum.lantern.domain.model.CrashLog
import kotlinx.coroutines.flow.Flow

interface CrashLogRepository {
    fun getCrashLog(): Flow<CrashLog?>
    fun hasCrashLog(): Boolean
    fun clearCrashLog()
}
