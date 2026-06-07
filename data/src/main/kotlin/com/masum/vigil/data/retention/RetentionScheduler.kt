package com.masum.vigil.data.retention

import com.masum.vigil.core.common.config.RetentionPolicy
import com.masum.vigil.core.common.dispatcher.DispatcherProvider
import com.masum.vigil.core.database.dao.DnsQueryDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetentionScheduler @Inject constructor(
    private val dnsQueryDao: DnsQueryDao,
    private val dispatchers: DispatcherProvider,
) {
    fun start(scope: CoroutineScope) {
        scope.launch(dispatchers.io) {
            while (isActive) {
                delay(PRUNE_INTERVAL_MS)
                prune()
            }
        }
    }

    suspend fun prune() {
        val cutoff = System.currentTimeMillis() - RetentionPolicy.DEFAULT.maxAge.inWholeMilliseconds
        dnsQueryDao.deleteOlderThan(cutoff)
        dnsQueryDao.trimToMaxRows(RetentionPolicy.DEFAULT.maxRows)
    }

    companion object {
        private const val PRUNE_INTERVAL_MS = 60 * 60 * 1_000L
    }
}
