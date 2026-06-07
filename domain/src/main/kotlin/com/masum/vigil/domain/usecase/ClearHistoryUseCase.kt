package com.masum.vigil.domain.usecase

import com.masum.vigil.domain.repository.DnsLogRepository
import javax.inject.Inject

class ClearHistoryUseCase @Inject constructor(
    private val repository: DnsLogRepository,
) {
    suspend operator fun invoke() = repository.clearHistory()
}
