package com.masum.lantern.domain.model

sealed class MonitoringState {
    data object Idle : MonitoringState()

    data class Active(
        val sessionId: Long,
        val startedAt: Long,
        val queryCount: Int = 0,
        val isPaused: Boolean = false,
        val pauseUntilTimestamp: Long? = null,
    ) : MonitoringState()

    data class Error(val message: String) : MonitoringState()
}
