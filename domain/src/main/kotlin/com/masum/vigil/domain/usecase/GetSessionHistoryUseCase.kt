package com.masum.vigil.domain.usecase

import com.masum.vigil.domain.model.SessionSummary
import com.masum.vigil.domain.repository.DnsLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSessionHistoryUseCase @Inject constructor(
    private val repository: DnsLogRepository,
) {
    operator fun invoke(): Flow<List<SessionSummary>> = repository.observeSessionHistory()
}
