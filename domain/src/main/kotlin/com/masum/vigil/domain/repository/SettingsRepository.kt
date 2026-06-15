package com.masum.vigil.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getThemeMode(): Flow<String>
    suspend fun setThemeMode(mode: String)

    fun getUpstreamDns(): Flow<String>
    suspend fun setUpstreamDns(dns: String)

    fun getMaxAgeDays(): Flow<Int>
    suspend fun setMaxAgeDays(days: Int)

    fun getMaxRows(): Flow<Int>
    suspend fun setMaxRows(rows: Int)
}
