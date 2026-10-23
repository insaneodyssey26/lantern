package com.masum.lantern.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.masum.lantern.core.database.entity.DnsQueryEntity
import com.masum.lantern.core.database.model.DomainStatTuple
import com.masum.lantern.core.database.model.PerAppStatTuple
import com.masum.lantern.core.database.model.SummaryTotals
import kotlinx.coroutines.flow.Flow

@Dao
interface DnsQueryDao {

    @Insert
    suspend fun insertAll(queries: List<DnsQueryEntity>)

    @Query("SELECT * FROM dns_queries ORDER BY timestamp DESC LIMIT 200")
    fun observeRecent(): Flow<List<DnsQueryEntity>>

    @Query("SELECT * FROM dns_queries ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<DnsQueryEntity>

    @Query("""
        SELECT appPackageName, appName, appUid,
               COUNT(*) AS totalQueries,
               SUM(CASE WHEN verdictType = 1 THEN 1 ELSE 0 END) AS trackerQueries
        FROM dns_queries
        WHERE (:sessionId IS NULL OR sessionId = :sessionId)
        GROUP BY appPackageName
        ORDER BY totalQueries DESC
    """)
    fun observePerAppStats(sessionId: Long?): Flow<List<PerAppStatTuple>>

    @Query("""
        SELECT COUNT(*) AS totalQueries,
               SUM(CASE WHEN verdictType = 1 THEN 1 ELSE 0 END) AS trackerQueries
        FROM dns_queries
    """)
    fun observeSummaryTotals(): Flow<SummaryTotals>

    @Query("""
        SELECT domain, COUNT(*) AS count,
               MAX(CASE WHEN verdictType = 1 THEN 1 ELSE 0 END) AS isTracker
        FROM dns_queries
        GROUP BY domain
        ORDER BY count DESC
        LIMIT 20
    """)
    fun observeTopDomains(): Flow<List<DomainStatTuple>>

    @Query("DELETE FROM dns_queries WHERE timestamp < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)

    @Query("""
        DELETE FROM dns_queries WHERE id NOT IN (
            SELECT id FROM dns_queries ORDER BY timestamp DESC LIMIT :maxRows
        )
    """)
    suspend fun trimToMaxRows(maxRows: Int)

    @Query("DELETE FROM dns_queries")
    suspend fun deleteAll()

    @Query("SELECT * FROM dns_queries WHERE appPackageName = :packageName ORDER BY timestamp DESC")
    fun observeByPackage(packageName: String): Flow<List<DnsQueryEntity>>

    @Query("SELECT * FROM dns_queries WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun observeBySession(sessionId: Long): Flow<List<DnsQueryEntity>>

    @Query("SELECT * FROM dns_queries WHERE sessionId = :sessionId AND appPackageName = :packageName ORDER BY timestamp DESC")
    fun observeBySessionAndPackage(sessionId: Long, packageName: String): Flow<List<DnsQueryEntity>>

    @Query("SELECT COUNT(*) FROM dns_queries")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM dns_queries")
    fun observeCount(): Flow<Int>
}
