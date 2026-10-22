package com.masum.lantern.feature.livefeed

import com.masum.lantern.core.ui.components.VerdictUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class LiveFeedUiState(
    val queries: ImmutableList<DnsQueryUiModel> = persistentListOf(),
    val isMonitoring: Boolean = false,
    val isPaused: Boolean = false,
    val userAllowedDomains: Set<String> = emptySet(),
    val userBlockedDomains: Set<String> = emptySet(),
)

data class DnsQueryUiModel(
    val id: Long,
    val domain: String,
    val appName: String,
    val packageName: String,
    val verdict: VerdictUiState,
    val time: String,
    val isBlocked: Boolean = false,
)
