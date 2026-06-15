package com.masum.vigil.data.retention

import com.masum.vigil.core.common.dispatcher.DispatcherProvider
import com.masum.vigil.core.database.dao.DnsQueryDao
import com.masum.vigil.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.days

@Singleton
class RetentionScheduler @Inject constructor(
    private val dnsQueryDao: DnsQueryDao,
    private val dispatchers: DispatcherProvider,
    private val settingsRepository: SettingsRepository,
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
        val maxAgeDays = settingsRepository.getMaxAgeDays().first()
        val maxRows = settingsRepository.getMaxRows().first()

        val cutoff = System.currentTimeMillis() - maxAgeDays.days.inWholeMilliseconds
        dnsQueryDao.deleteOlderThan(cutoff)
        dnsQueryDao.trimToMaxRows(maxRows)
    }

    companion object {
        private const val PRUNE_INTERVAL_MS = 60 * 60 * 1_000L
    }
}
