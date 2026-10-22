package com.masum.lantern.feature.whitelist

import com.masum.lantern.domain.model.AppInfo

data class AppWhitelistUiState(
    val apps: List<WhitelistAppItem> = emptyList(),
    val bypassedPackages: Set<String> = emptySet(),
    val isLoading: Boolean = true,
)

data class WhitelistAppItem(
    val appInfo: AppInfo,
    val isBypassed: Boolean,
)
