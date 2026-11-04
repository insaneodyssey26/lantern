package com.masum.lantern.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.masum.lantern.core.database.entity.BlocklistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlocklistDao {
    @Query("SELECT * FROM blocklists ORDER BY isCustom ASC, category ASC, name ASC")
    fun observeAll(): Flow<List<BlocklistEntity>>

    @Query("SELECT * FROM blocklists ORDER BY isCustom ASC, category ASC, name ASC")
    suspend fun getAll(): List<BlocklistEntity>

    @Query("SELECT * FROM blocklists WHERE isEnabled = 1")
    suspend fun getEnabled(): List<BlocklistEntity>

    @Query("SELECT * FROM blocklists WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): BlocklistEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaults(lists: List<BlocklistEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(list: BlocklistEntity)

    @Update
    suspend fun update(list: BlocklistEntity)

    @Query("UPDATE blocklists SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun updateEnabled(id: String, isEnabled: Boolean)

    @Query("UPDATE blocklists SET ruleCount = :ruleCount, lastUpdated = :lastUpdated, etag = :etag, lastModifiedHeader = :lastModifiedHeader WHERE id = :id")
    suspend fun updateMetadata(id: String, ruleCount: Int, lastUpdated: Long, etag: String?, lastModifiedHeader: String?)

    @Query("UPDATE blocklists SET url = :url, sourceUrl = :sourceUrl WHERE id = :id AND isCustom = 0")
    suspend fun updatePresetUrl(id: String, url: String, sourceUrl: String)

    @Query("DELETE FROM blocklists WHERE id = :id AND isCustom = 1")
    suspend fun deleteCustom(id: String)
}
