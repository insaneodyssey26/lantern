package com.masum.lantern.domain.repository

import com.masum.lantern.domain.model.Blocklist
import com.masum.lantern.domain.model.Verdict
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface BlocklistRepository {
    fun classify(domain: String): Verdict
    fun domainCount(): Int
    fun isReady(): Boolean
    suspend fun updateBlocklist(): Result<Boolean>
    fun getLastUpdatedTimestamp(): Long

    fun observeBlocklists(): Flow<List<Blocklist>>
    fun observeTotalRuleCount(): StateFlow<Int>
    suspend fun toggleBlocklist(id: String, isEnabled: Boolean)
    suspend fun addCustomBlocklist(name: String, url: String): Result<Unit>
    suspend fun deleteCustomBlocklist(id: String): Result<Unit>
    suspend fun syncAllBlocklists(): Result<Int>
    suspend fun syncBlocklist(id: String): Result<Boolean>
}
