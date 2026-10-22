package com.masum.lantern.domain.usecase

import com.masum.lantern.domain.repository.DnsLogRepository
import javax.inject.Inject

class ClearHistoryUseCase @Inject constructor(
    private val repository: DnsLogRepository,
) {
    suspend operator fun invoke() = repository.clearHistory()
}
