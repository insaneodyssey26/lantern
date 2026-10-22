package com.masum.lantern.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getThemeMode(): Flow<String>
    suspend fun setThemeMode(mode: String)

    // Color palette key: "system" | "vigil" | "storm" | "ember" | "orchid"
    fun getColorTheme(): Flow<String>
    suspend fun setColorTheme(theme: String)

    fun getUpstreamDns(): Flow<String>
    suspend fun setUpstreamDns(dns: String)

    fun getMaxAgeDays(): Flow<Int>
    suspend fun setMaxAgeDays(days: Int)

    fun getMaxRows(): Flow<Int>
    suspend fun setMaxRows(rows: Int)

    fun isBlockingEnabled(): Flow<Boolean>
    suspend fun setBlockingEnabled(enabled: Boolean)

    fun isUseSystemFont(): Flow<Boolean>
    suspend fun setUseSystemFont(useSystem: Boolean)
}
