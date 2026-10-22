package com.masum.lantern.domain.usecase

import com.masum.lantern.domain.model.MonitoringState
import com.masum.lantern.domain.repository.MonitoringRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMonitoringStateUseCase @Inject constructor(
    private val repository: MonitoringRepository,
) {
    operator fun invoke(): Flow<MonitoringState> = repository.observeMonitoringState()
}
