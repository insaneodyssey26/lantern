package com.masum.lantern.domain.usecase

import com.masum.lantern.domain.model.SessionSummary
import com.masum.lantern.domain.repository.DnsLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSessionHistoryUseCase @Inject constructor(
    private val repository: DnsLogRepository,
) {
    operator fun invoke(): Flow<List<SessionSummary>> = repository.observeSessionHistory()
}
