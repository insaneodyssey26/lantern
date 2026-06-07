package com.masum.vigil.feature.livefeed

import com.masum.vigil.core.ui.components.VerdictUiState

data class LiveFeedUiState(
    val queries: List<DnsQueryUiModel> = emptyList(),
    val isMonitoring: Boolean = false,
)

data class DnsQueryUiModel(
    val id: Long,
    val domain: String,
    val appName: String,
    val verdict: VerdictUiState,
    val time: String,
)
