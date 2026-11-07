package com.masum.lantern.domain.usecase

import com.masum.lantern.domain.repository.MonitoringRepository
import javax.inject.Inject

class PauseMonitoringUseCase @Inject constructor(
    private val monitoringRepository: MonitoringRepository,
) {
    suspend operator fun invoke(durationMs: Long) = monitoringRepository.pauseMonitoring(durationMs)
}
