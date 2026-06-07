package com.masum.vigil.domain.usecase

import com.masum.vigil.domain.model.PerAppStat
import com.masum.vigil.domain.repository.DnsLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPerAppStatsUseCase @Inject constructor(
    private val repository: DnsLogRepository,
) {
    operator fun invoke(sessionId: Long? = null): Flow<List<PerAppStat>> =
        repository.observePerAppStats(sessionId)
}
