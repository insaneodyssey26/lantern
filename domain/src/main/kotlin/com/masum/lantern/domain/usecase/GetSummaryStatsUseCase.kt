package com.masum.lantern.domain.usecase

import com.masum.lantern.domain.model.SummaryStats
import com.masum.lantern.domain.repository.DnsLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSummaryStatsUseCase @Inject constructor(
    private val repository: DnsLogRepository,
) {
    operator fun invoke(): Flow<SummaryStats> = repository.observeSummaryStats()
}
