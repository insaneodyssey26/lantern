package com.masum.lantern.feature.control

import android.content.Context
import android.net.ConnectivityManager
import app.cash.turbine.test
import com.masum.lantern.domain.model.MonitoringState
import com.masum.lantern.domain.model.SummaryStats
import com.masum.lantern.domain.repository.BlocklistRepository
import com.masum.lantern.domain.repository.SettingsRepository
import com.masum.lantern.domain.usecase.GetSummaryStatsUseCase
import com.masum.lantern.domain.usecase.ObserveMonitoringStateUseCase
import com.masum.lantern.domain.usecase.StartMonitoringUseCase
import com.masum.lantern.domain.usecase.StopMonitoringUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ControlViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val context: Context = mockk(relaxed = true)
    private val startMonitoring: StartMonitoringUseCase = mockk(relaxed = true)
    private val stopMonitoring: StopMonitoringUseCase = mockk(relaxed = true)
    private val observeMonitoringState: ObserveMonitoringStateUseCase = mockk()
    private val getSummaryStats: GetSummaryStatsUseCase = mockk()
    private val blocklistRepository: BlocklistRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)

    private val monitoringFlow = MutableStateFlow<MonitoringState>(MonitoringState.Idle)
    private val statsFlow = MutableStateFlow(SummaryStats(totalQueries = 100, trackerQueries = 25, topDomains = emptyList()))
    private val blockingEnabledFlow = MutableStateFlow(true)
    private val upstreamDnsFlow = MutableStateFlow("1.1.1.1")

    private lateinit var viewModel: ControlViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        val connectivityManager: ConnectivityManager = mockk(relaxed = true)
        every { connectivityManager.activeNetwork } returns null
        every { context.getSystemService(ConnectivityManager::class.java) } returns connectivityManager

        every { observeMonitoringState() } returns monitoringFlow
        every { getSummaryStats() } returns statsFlow
        every { blocklistRepository.domainCount() } returns 82000
        every { settingsRepository.isBlockingEnabled() } returns blockingEnabledFlow
        every { settingsRepository.getUpstreamDns() } returns upstreamDnsFlow

        viewModel = ControlViewModel(
            context = context,
            startMonitoring = startMonitoring,
            stopMonitoring = stopMonitoring,
            observeMonitoringState = observeMonitoringState,
            getSummaryStats = getSummaryStats,
            blocklistRepository = blocklistRepository,
            settingsRepository = settingsRepository
        )
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state reflects repository values`() = runTest {
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(82000, state.blocklistCount)
            assertEquals(100, state.totalQueries)
            assertEquals(25, state.trackerQueries)
            assertTrue(state.isBlockingEnabled)
            assertEquals("1.1.1.1", state.upstreamDns)
            assertEquals(MonitoringState.Idle, state.monitoringState)
        }
    }

    @Test
    fun `monitoring state updates reactively`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            monitoringFlow.value = MonitoringState.Active(sessionId = 99L, startedAt = 1000L)
            val state = awaitItem()
            assertTrue(state.monitoringState is MonitoringState.Active)
            assertEquals(99L, (state.monitoringState as MonitoringState.Active).sessionId)
        }
    }

    @Test
    fun `setBlockingEnabled delegates to settings repository`() = runTest {
        viewModel.setBlockingEnabled(false)
        coVerify { settingsRepository.setBlockingEnabled(false) }
    }

    @Test
    fun `onToggleClick stops monitoring when currently active`() = runTest {
        monitoringFlow.value = MonitoringState.Active(sessionId = 1L, startedAt = 1000L)

        viewModel.onToggleClick()

        coVerify { stopMonitoring() }
        coVerify(exactly = 0) { startMonitoring() }
    }

    @Test
    fun `onConsentGranted starts monitoring and clears pending intent`() = runTest {
        coEvery { startMonitoring() } returns Result.success(Unit)

        viewModel.onConsentGranted()

        assertEquals(null, viewModel.uiState.value.pendingConsentIntent)
        coVerify { startMonitoring() }
    }

    @Test
    fun `onConsentDismissed clears pending intent`() {
        viewModel.onConsentDismissed()
        assertEquals(null, viewModel.uiState.value.pendingConsentIntent)
    }
}
