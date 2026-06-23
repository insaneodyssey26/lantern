package com.masum.vigil.feature.livefeed

import com.masum.vigil.core.ui.components.VerdictUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class LiveFeedUiState(
    val queries: ImmutableList<DnsQueryUiModel> = persistentListOf(),
    val isMonitoring: Boolean = false,
)

data class DnsQueryUiModel(
    val id: Long,
    val domain: String,
    val appName: String,
    val packageName: String,
    val verdict: VerdictUiState,
    val time: String,
)
