package com.masum.lantern.feature.settings

data class SettingsUiState(
    val themeMode: String = "system",
    val colorTheme: String = "system",
    val pureBlack: Boolean = false,
    val useSystemFont: Boolean = false,
    val upstreamDns: String = "1.1.1.1",
    val maxAgeDays: Int = 7,
    val maxRows: Int = 50_000,
    val totalLogCount: Long = 0,
    val blocklistCount: Int = 0,
    val blocklistLastUpdated: Long = 0L,
    val isClearingLogs: Boolean = false,
    val isUpdatingBlocklist: Boolean = false,
    val blocklistUpdateMessage: String? = null,
    val bypassedAppsCount: Int = 0,
)

