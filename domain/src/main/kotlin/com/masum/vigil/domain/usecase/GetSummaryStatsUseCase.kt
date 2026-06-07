package com.masum.vigil.domain.usecase

import com.masum.vigil.domain.model.SummaryStats
import com.masum.vigil.domain.repository.DnsLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSummaryStatsUseCase @Inject constructor(
    private val repository: DnsLogRepository,
) {
    operator fun invoke(): Flow<SummaryStats> = repository.observeSummaryStats()
}
