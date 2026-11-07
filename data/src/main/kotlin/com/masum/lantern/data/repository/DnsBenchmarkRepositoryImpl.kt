package com.masum.lantern.data.repository

import com.masum.lantern.core.common.config.ResolverConfig
import com.masum.lantern.data.benchmark.DnsBenchmarkEngine
import com.masum.lantern.domain.model.BenchmarkStatus
import com.masum.lantern.domain.model.DnsBenchmarkItem
import com.masum.lantern.domain.repository.DnsBenchmarkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DnsBenchmarkRepositoryImpl @Inject constructor() : DnsBenchmarkRepository {

    private val engine = DnsBenchmarkEngine()
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var benchmarkJob: Job? = null

    private val initialProviders = listOf(
        DnsBenchmarkItem("cloudflare", "Cloudflare", ResolverConfig.PRIMARY_DNS),
        DnsBenchmarkItem("quad9", "Quad9", ResolverConfig.FALLBACK_DNS),
        DnsBenchmarkItem("google", "Google Public DNS", ResolverConfig.GOOGLE_DNS),
        DnsBenchmarkItem("adguard", "AdGuard DNS", ResolverConfig.ADGUARD_DNS)
    )

    private val _benchmarkResults = MutableStateFlow(initialProviders)
    override val benchmarkResults: Flow<List<DnsBenchmarkItem>> = _benchmarkResults.asStateFlow()

    private val _isBenchmarking = MutableStateFlow(false)
    override val isBenchmarking: Flow<Boolean> = _isBenchmarking.asStateFlow()

    override suspend fun runBenchmark(protocol: String) {
        benchmarkJob?.cancel()
        benchmarkJob = scope.launch {
            _isBenchmarking.value = true
            _benchmarkResults.value = _benchmarkResults.value.map {
                it.copy(
                    status = BenchmarkStatus.RUNNING,
                    latencyMs = null,
                    minLatencyMs = null,
                    maxLatencyMs = null,
                    jitterMs = null,
                    isFastest = false
                )
            }

            val currentItems = _benchmarkResults.value
            val deferredProbes = currentItems.map { provider ->
                async {
                    val result = engine.probeServer(
                        providerId = provider.providerId,
                        name = provider.name,
                        ip = provider.ip,
                        protocol = protocol,
                        pings = 3
                    )
                    updateSingleResult(result)
                    result
                }
            }

            val completed = deferredProbes.awaitAll()
            val successful = completed.filter { it.status == BenchmarkStatus.SUCCESS && it.latencyMs != null }
            val minLatency = successful.minOfOrNull { it.latencyMs ?: Long.MAX_VALUE }

            val fastestId = if (minLatency != null) {
                successful
                    .filter { it.latencyMs == minLatency }
                    .minByOrNull { it.jitterMs ?: Long.MAX_VALUE }
                    ?.providerId
            } else null

            _benchmarkResults.value = completed.map { item ->
                item.copy(isFastest = (fastestId != null && item.providerId == fastestId))
            }.sortedWith(
                compareBy(
                    { it.status != BenchmarkStatus.SUCCESS },
                    { it.latencyMs ?: Long.MAX_VALUE },
                    { it.jitterMs ?: Long.MAX_VALUE }
                )
            )

            _isBenchmarking.value = false
        }
    }

    private fun updateSingleResult(updated: DnsBenchmarkItem) {
        _benchmarkResults.value = _benchmarkResults.value.map { current ->
            if (current.providerId == updated.providerId) updated else current
        }
    }

    override fun stopBenchmark() {
        benchmarkJob?.cancel()
        benchmarkJob = null
        _isBenchmarking.value = false
    }
}
