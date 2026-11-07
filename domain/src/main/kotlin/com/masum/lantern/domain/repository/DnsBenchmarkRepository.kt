package com.masum.lantern.domain.repository

import com.masum.lantern.domain.model.DnsBenchmarkItem
import kotlinx.coroutines.flow.Flow

interface DnsBenchmarkRepository {
    val benchmarkResults: Flow<List<DnsBenchmarkItem>>
    val isBenchmarking: Flow<Boolean>
    suspend fun runBenchmark(protocol: String)
    fun stopBenchmark()
}
