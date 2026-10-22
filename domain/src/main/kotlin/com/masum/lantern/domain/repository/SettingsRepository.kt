package com.masum.lantern.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getThemeMode(): Flow<String>
    fun getThemeModeSync(): String
    suspend fun setThemeMode(mode: String)

    fun getColorTheme(): Flow<String>
    fun getColorThemeSync(): String
    suspend fun setColorTheme(theme: String)

    fun getUpstreamDns(): Flow<String>
    suspend fun setUpstreamDns(dns: String)

    fun getMaxAgeDays(): Flow<Int>
    suspend fun setMaxAgeDays(days: Int)

    fun getMaxRows(): Flow<Int>
    suspend fun setMaxRows(rows: Int)

    fun isBlockingEnabled(): Flow<Boolean>
    suspend fun setBlockingEnabled(enabled: Boolean)

    fun isPureBlack(): Flow<Boolean>
    fun isPureBlackSync(): Boolean
    suspend fun setPureBlack(enabled: Boolean)

    fun isUseSystemFont(): Flow<Boolean>
    fun isUseSystemFontSync(): Boolean
    suspend fun setUseSystemFont(useSystem: Boolean)

    fun getBypassedApps(): Flow<Set<String>>
    suspend fun setAppBypassed(packageName: String, bypassed: Boolean)

    fun getUserAllowedDomains(): Flow<Set<String>>
    fun getUserAllowedDomainsSync(): Set<String>
    suspend fun setUserDomainAllowed(domain: String, allowed: Boolean)

    fun getUserBlockedDomains(): Flow<Set<String>>
    fun getUserBlockedDomainsSync(): Set<String>
    suspend fun setUserDomainBlocked(domain: String, blocked: Boolean)
}
