package com.masum.lantern.domain.repository

import com.masum.lantern.domain.model.DnsQuery
import com.masum.lantern.domain.model.PerAppStat
import com.masum.lantern.domain.model.SessionSummary
import com.masum.lantern.domain.model.SummaryStats
import kotlinx.coroutines.flow.Flow

interface DnsLogRepository {
    fun observeLiveFeed(): Flow<DnsQuery>
    suspend fun getRecentQueries(limit: Int = 100): List<DnsQuery>
    fun observeSessionHistory(): Flow<List<SessionSummary>>
    fun observeSession(sessionId: Long): Flow<SessionSummary?>
    fun observePerAppStats(sessionId: Long? = null): Flow<List<PerAppStat>>
    fun observeSummaryStats(): Flow<SummaryStats>
    fun observeTotalCount(): Flow<Int>
    fun observeAppQueries(packageName: String): Flow<List<DnsQuery>>
    fun observeSessionQueries(sessionId: Long): Flow<List<DnsQuery>>
    fun observeSessionAppQueries(sessionId: Long, packageName: String): Flow<List<DnsQuery>>
    suspend fun clearHistory()
}
