package com.masum.vigil.domain.usecase

import com.masum.vigil.domain.model.DnsQuery
import com.masum.vigil.domain.repository.DnsLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveLiveFeedUseCase @Inject constructor(
    private val repository: DnsLogRepository,
) {
    operator fun invoke(): Flow<DnsQuery> = repository.observeLiveFeed()
}
