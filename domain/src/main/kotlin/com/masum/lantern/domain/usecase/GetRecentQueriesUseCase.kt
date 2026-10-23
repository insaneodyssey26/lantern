package com.masum.lantern.domain.usecase

import com.masum.lantern.domain.model.DnsQuery
import com.masum.lantern.domain.repository.DnsLogRepository
import javax.inject.Inject

class GetRecentQueriesUseCase @Inject constructor(
    private val repository: DnsLogRepository,
) {
    suspend operator fun invoke(limit: Int = 100): List<DnsQuery> = repository.getRecentQueries(limit)
}
