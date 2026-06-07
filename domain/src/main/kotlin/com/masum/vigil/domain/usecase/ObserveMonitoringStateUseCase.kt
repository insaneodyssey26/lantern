package com.masum.vigil.domain.usecase

import com.masum.vigil.domain.model.MonitoringState
import com.masum.vigil.domain.repository.MonitoringRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMonitoringStateUseCase @Inject constructor(
    private val repository: MonitoringRepository,
) {
    operator fun invoke(): Flow<MonitoringState> = repository.observeMonitoringState()
}
