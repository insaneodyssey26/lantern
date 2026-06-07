package com.masum.vigil.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.masum.vigil.core.database.entity.SessionEntity
import com.masum.vigil.core.database.model.SessionWithCountsTuple
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert
    suspend fun insert(session: SessionEntity): Long

    @Query("UPDATE sessions SET endedAt = :endedAt WHERE id = :sessionId")
    suspend fun updateEndTime(sessionId: Long, endedAt: Long)

    @Query("SELECT * FROM sessions ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<SessionEntity>>

    @Query("""
        SELECT s.id AS sessionId, s.startedAt, s.endedAt,
               COUNT(q.id) AS totalQueries,
               SUM(CASE WHEN q.verdictType = 1 THEN 1 ELSE 0 END) AS trackerQueries
        FROM sessions s
        LEFT JOIN dns_queries q ON q.sessionId = s.id
        GROUP BY s.id
        ORDER BY s.startedAt DESC
    """)
    fun observeAllWithCounts(): Flow<List<SessionWithCountsTuple>>

    @Query("DELETE FROM sessions")
    suspend fun deleteAll()
}
