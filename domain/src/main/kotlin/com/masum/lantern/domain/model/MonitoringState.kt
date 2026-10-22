package com.masum.lantern.domain.model

sealed class MonitoringState {
    data object Idle : MonitoringState()

    data class Active(
        val sessionId: Long,
        val startedAt: Long,
        val queryCount: Int = 0,
    ) : MonitoringState()

    data class Error(val message: String) : MonitoringState()
}
