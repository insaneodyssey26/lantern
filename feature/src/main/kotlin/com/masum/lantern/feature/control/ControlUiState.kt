package com.masum.lantern.feature.control

import android.content.Intent
import com.masum.lantern.domain.model.MonitoringState

data class ControlUiState(
    val monitoringState: MonitoringState = MonitoringState.Idle,
    val totalQueries: Int = 0,
    val trackerQueries: Int = 0,
    val blockedQueries: Int = 0,
    val isPrivateDnsActive: Boolean = false,
    val pendingConsentIntent: Intent? = null,
    val blocklistCount: Int = 0,
    val isBlockingEnabled: Boolean = true,
    val upstreamDns: String = "1.1.1.1",
    val isFeatureGuideCompleted: Boolean = true,
    val showWhatsNewSheet: Boolean = false,
    val dnsProtocol: String = "UDP",
    val isBenchmarking: Boolean = false,
    val benchmarkResults: List<com.masum.lantern.domain.model.DnsBenchmarkItem> = emptyList(),
)
