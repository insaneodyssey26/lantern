package com.masum.vigil.domain.usecase

import com.masum.vigil.domain.repository.MonitoringRepository
import javax.inject.Inject

class StartMonitoringUseCase @Inject constructor(
    private val repository: MonitoringRepository,
) {
    suspend operator fun invoke(): Result<Unit> = repository.startMonitoring()
}
