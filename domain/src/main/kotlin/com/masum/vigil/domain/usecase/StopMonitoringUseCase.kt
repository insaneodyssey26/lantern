package com.masum.vigil.domain.usecase

import com.masum.vigil.domain.repository.MonitoringRepository
import javax.inject.Inject

class StopMonitoringUseCase @Inject constructor(
    private val repository: MonitoringRepository,
) {
    suspend operator fun invoke() = repository.stopMonitoring()
}
