package com.masum.vigil.domain.usecase

import com.masum.vigil.domain.model.MonitoringState
import com.masum.vigil.domain.repository.MonitoringRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StartMonitoringUseCaseTest {

    private val fakeRepository = object : MonitoringRepository {
        var startCalled = false
        var startResult: Result<Unit> = Result.success(Unit)

        override suspend fun startMonitoring(): Result<Unit> {
            startCalled = true
            return startResult
        }

        override suspend fun stopMonitoring() = Unit

        override fun observeMonitoringState(): Flow<MonitoringState> =
            flowOf(MonitoringState.Idle)
    }

    private val useCase = StartMonitoringUseCase(fakeRepository)

    @Test
    fun `invoke delegates to repository and returns success`() = runTest {
        val result = useCase()
        assertTrue(fakeRepository.startCalled)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        fakeRepository.startResult = Result.failure(IllegalStateException("VPN permission missing"))
        val result = useCase()
        assertTrue(result.isFailure)
        assertEquals("VPN permission missing", result.exceptionOrNull()?.message)
    }
}
