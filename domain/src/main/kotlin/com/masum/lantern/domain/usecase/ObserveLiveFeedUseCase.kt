package com.masum.lantern.domain.usecase

import com.masum.lantern.domain.model.DnsQuery
import com.masum.lantern.domain.repository.DnsLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveLiveFeedUseCase @Inject constructor(
    private val repository: DnsLogRepository,
) {
    operator fun invoke(): Flow<DnsQuery> = repository.observeLiveFeed()
}
