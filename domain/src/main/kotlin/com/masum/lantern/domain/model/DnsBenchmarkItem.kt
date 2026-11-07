package com.masum.lantern.domain.model

enum class BenchmarkStatus {
    IDLE,
    RUNNING,
    SUCCESS,
    FAILED,
}

data class DnsBenchmarkItem(
    val providerId: String,
    val name: String,
    val ip: String,
    val status: BenchmarkStatus = BenchmarkStatus.IDLE,
    val latencyMs: Long? = null,
    val minLatencyMs: Long? = null,
    val maxLatencyMs: Long? = null,
    val jitterMs: Long? = null,
    val successPings: Int = 0,
    val totalPings: Int = 3,
    val isFastest: Boolean = false,
)
