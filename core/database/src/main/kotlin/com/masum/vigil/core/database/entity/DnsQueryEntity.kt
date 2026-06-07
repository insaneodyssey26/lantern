package com.masum.vigil.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dns_queries",
    indices = [
        Index(value = ["sessionId"]),
        Index(value = ["domain"]),
    ],
)
data class DnsQueryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val domain: String,
    val timestamp: Long,
    val verdictType: Int,
    val verdictCategory: String,
    val appUid: Int,
    val appPackageName: String,
    val appName: String,
) {
    companion object {
        const val VERDICT_CLEAN = 0
        const val VERDICT_TRACKER = 1
        const val VERDICT_UNKNOWN = 2
    }
}
