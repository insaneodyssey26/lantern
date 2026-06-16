package com.masum.vigil.domain.repository

import com.masum.vigil.domain.model.DnsQuery
import com.masum.vigil.domain.model.PerAppStat
import com.masum.vigil.domain.model.SessionSummary
import com.masum.vigil.domain.model.SummaryStats
import kotlinx.coroutines.flow.Flow

interface DnsLogRepository {
    fun observeLiveFeed(): Flow<DnsQuery>
    fun observeSessionHistory(): Flow<List<SessionSummary>>
    fun observePerAppStats(sessionId: Long? = null): Flow<List<PerAppStat>>
    fun observeSummaryStats(): Flow<SummaryStats>
    fun observeTotalCount(): Flow<Int>
    fun observeAppQueries(packageName: String): Flow<List<DnsQuery>>
    suspend fun clearHistory()
}
