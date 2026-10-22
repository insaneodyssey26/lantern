package com.masum.lantern.data.repository

import com.masum.lantern.core.common.dispatcher.DispatcherProvider
import com.masum.lantern.core.database.dao.DnsQueryDao
import com.masum.lantern.core.database.dao.SessionDao
import com.masum.lantern.core.database.entity.DnsQueryEntity
import com.masum.lantern.data.mapper.toDomain
import com.masum.lantern.data.mapper.toEntity
import com.masum.lantern.data.mapper.toPerAppStat
import com.masum.lantern.data.mapper.toSessionSummary
import com.masum.lantern.data.mapper.toSummaryStats
import com.masum.lantern.data.retention.RetentionScheduler
import com.masum.lantern.domain.model.DnsQuery
import com.masum.lantern.domain.model.PerAppStat
import com.masum.lantern.domain.model.SessionSummary
import com.masum.lantern.domain.model.SummaryStats
import com.masum.lantern.domain.repository.DnsLogRepository
import com.masum.lantern.vpn.session.MonitoringSessionBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DnsLogRepositoryImpl @Inject constructor(
    private val bus: MonitoringSessionBus,
    private val dnsQueryDao: DnsQueryDao,
    private val sessionDao: SessionDao,
    private val retentionScheduler: RetentionScheduler,
    private val dispatchers: DispatcherProvider,
) : DnsLogRepository {

    private val scope = CoroutineScope(SupervisorJob() + dispatchers.io)
    private val batchMutex = Mutex()
    private val pendingBatch = ArrayList<DnsQueryEntity>(BATCH_SIZE)

    init {
        scope.launch {
            sessionDao.closeAllActiveSessions(System.currentTimeMillis())
        }
        scope.launch { collectAndBatchWrite() }
        scope.launch { periodicFlush() }
        retentionScheduler.start(scope)
    }

    private suspend fun collectAndBatchWrite() {
        bus.queries.collect { query ->
            val shouldFlush = batchMutex.withLock {
                pendingBatch.add(query.toEntity())
                pendingBatch.size >= BATCH_SIZE
            }
            if (shouldFlush) flushBatch()
        }
    }

    private suspend fun periodicFlush() {
        while (true) {
            kotlinx.coroutines.delay(FLUSH_INTERVAL_MS)
            flushBatch()
        }
    }

    private suspend fun flushBatch() {
        val toWrite = batchMutex.withLock {
            if (pendingBatch.isEmpty()) return
            ArrayList(pendingBatch).also { pendingBatch.clear() }
        }
        dnsQueryDao.insertAll(toWrite)
    }

    override fun observeLiveFeed(): Flow<DnsQuery> = bus.queries

    override fun observeSessionHistory(): Flow<List<SessionSummary>> =
        sessionDao.observeAllWithCounts().map { list ->
            list.map { it.toSessionSummary() }
        }

    override fun observePerAppStats(sessionId: Long?): Flow<List<PerAppStat>> =
        dnsQueryDao.observePerAppStats(sessionId).map { list ->
            list.map { it.toPerAppStat() }
        }

    override fun observeSummaryStats(): Flow<SummaryStats> =
        combine(
            dnsQueryDao.observeSummaryTotals(),
            dnsQueryDao.observeTopDomains(),
        ) { totals, domains ->
            totals.toSummaryStats(domains)
        }

    override fun observeTotalCount(): Flow<Int> = dnsQueryDao.observeCount()

    override fun observeAppQueries(packageName: String): Flow<List<DnsQuery>> =
        dnsQueryDao.observeByPackage(packageName).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun clearHistory(): Unit = withContext(dispatchers.io) {
        dnsQueryDao.deleteAll()
        sessionDao.deleteAll()
    }

    companion object {
        private const val BATCH_SIZE = 50
        private const val FLUSH_INTERVAL_MS = 2_000L
    }
}
