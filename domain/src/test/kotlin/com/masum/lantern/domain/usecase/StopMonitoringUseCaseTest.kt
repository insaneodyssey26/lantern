package com.masum.lantern.domain.usecase

import com.masum.lantern.domain.model.MonitoringState
import com.masum.lantern.domain.repository.MonitoringRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class StopMonitoringUseCaseTest {

    private val fakeRepository = object : MonitoringRepository {
        var stopCalled = false

        override suspend fun startMonitoring(): Result<Unit> = Result.success(Unit)

        override suspend fun stopMonitoring() {
            stopCalled = true
        }

        override fun observeMonitoringState(): Flow<MonitoringState> =
            flowOf(MonitoringState.Active(sessionId = 1L, startedAt = 0L))
    }

    private val useCase = StopMonitoringUseCase(fakeRepository)

    @Test
    fun `invoke delegates to repository`() = runTest {
        useCase()
        assertTrue(fakeRepository.stopCalled)
    }
}
