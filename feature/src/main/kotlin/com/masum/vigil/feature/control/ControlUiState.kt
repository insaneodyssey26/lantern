package com.masum.vigil.feature.control

import android.content.Intent
import com.masum.vigil.domain.model.MonitoringState

data class ControlUiState(
    val monitoringState: MonitoringState = MonitoringState.Idle,
    val totalQueries: Int = 0,
    val trackerQueries: Int = 0,
    val isPrivateDnsActive: Boolean = false,
    val pendingConsentIntent: Intent? = null,
)
