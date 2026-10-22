package com.masum.lantern.domain.usecase

import com.masum.lantern.domain.repository.MonitoringRepository
import javax.inject.Inject

class StopMonitoringUseCase @Inject constructor(
    private val repository: MonitoringRepository,
) {
    suspend operator fun invoke() = repository.stopMonitoring()
}
