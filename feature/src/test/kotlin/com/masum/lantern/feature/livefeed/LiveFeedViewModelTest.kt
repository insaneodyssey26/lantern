package com.masum.lantern.feature.livefeed

import app.cash.turbine.test
import com.masum.lantern.domain.model.AppInfo
import com.masum.lantern.domain.model.DnsQuery
import com.masum.lantern.domain.model.MonitoringState
import com.masum.lantern.domain.model.Verdict
import com.masum.lantern.domain.usecase.ObserveLiveFeedUseCase
import com.masum.lantern.domain.usecase.ObserveMonitoringStateUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
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

class LiveFeedViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val queryFlow = MutableSharedFlow<DnsQuery>()
    private val stateFlow = MutableStateFlow<MonitoringState>(MonitoringState.Idle)

    private val observeLiveFeed: ObserveLiveFeedUseCase = mockk()
    private val observeMonitoringState: ObserveMonitoringStateUseCase = mockk()
    private val settingsRepository: com.masum.lantern.domain.repository.SettingsRepository = mockk(relaxed = true)

    private lateinit var viewModel: LiveFeedViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { observeLiveFeed() } returns queryFlow
        every { observeMonitoringState() } returns stateFlow
        every { settingsRepository.getUserAllowedDomains() } returns MutableStateFlow(emptySet())
        every { settingsRepository.getUserBlockedDomains() } returns MutableStateFlow(emptySet())
        viewModel = LiveFeedViewModel(observeLiveFeed, observeMonitoringState, settingsRepository)
    }

    @After
    fun teardown() = Dispatchers.resetMain()

    private fun query(domain: String, id: Long = 0L) = DnsQuery(
        id = id,
        sessionId = 1L,
        domain = domain,
        timestamp = System.currentTimeMillis(),
        verdict = Verdict.Clean,
        appInfo = AppInfo.UNKNOWN,
    )

    @Test
    fun `initial state has empty queries and not monitoring`() {
        assertFalse(viewModel.uiState.value.isMonitoring)
        assertTrue(viewModel.uiState.value.queries.isEmpty())
    }

    @Test
    fun `monitoring state updates when active`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            stateFlow.value = MonitoringState.Active(sessionId = 1L, startedAt = 0L)
            assertTrue(awaitItem().isMonitoring)
        }
    }

    @Test
    fun `incoming query appears in feed`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            queryFlow.emit(query("google.com"))
            val state = awaitItem()
            assertEquals(1, state.queries.size)
            assertEquals("google.com", state.queries[0].domain)
        }
    }

    @Test
    fun `queries are prepended so newest is first`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            queryFlow.emit(query("first.com"))
            awaitItem()
            queryFlow.emit(query("second.com"))
            val state = awaitItem()
            assertEquals("second.com", state.queries[0].domain)
            assertEquals("first.com", state.queries[1].domain)
        }
    }

    @Test
    fun `buffer is capped at 100 items`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            repeat(105) { i -> queryFlow.emit(query("domain$i.com", id = i.toLong())) }
            val state = expectMostRecentItem()
            assertEquals(100, state.queries.size)
        }
    }
}
