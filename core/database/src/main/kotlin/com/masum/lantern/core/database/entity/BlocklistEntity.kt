package com.masum.lantern.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocklists")
data class BlocklistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val category: String,
    val url: String,
    val isEnabled: Boolean,
    val isCustom: Boolean,
    val ruleCount: Int,
    val lastUpdated: Long,
    val lastModifiedHeader: String? = null,
    val etag: String? = null,
    val sourceUrl: String? = null
)
