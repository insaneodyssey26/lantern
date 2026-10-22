package com.masum.lantern.feature.stats

import app.cash.turbine.test
import com.masum.lantern.domain.model.AppInfo
import com.masum.lantern.domain.model.DomainStat
import com.masum.lantern.domain.model.PerAppStat
import com.masum.lantern.domain.model.SessionSummary
import com.masum.lantern.domain.model.SummaryStats
import com.masum.lantern.domain.repository.BlocklistRepository
import com.masum.lantern.domain.repository.DnsLogRepository
import com.masum.lantern.domain.usecase.ClearHistoryUseCase
import com.masum.lantern.domain.usecase.GetPerAppStatsUseCase
import com.masum.lantern.domain.usecase.GetSessionHistoryUseCase
import com.masum.lantern.domain.usecase.GetSummaryStatsUseCase
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
import org.junit.Before
import org.junit.Test

class StatsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val statsFlow = MutableStateFlow(SummaryStats.EMPTY)
    private val perAppFlow = MutableStateFlow(emptyList<PerAppStat>())
    private val sessionFlow = MutableStateFlow(emptyList<SessionSummary>())

    private val getSummaryStats: GetSummaryStatsUseCase = mockk()
    private val getPerAppStats: GetPerAppStatsUseCase = mockk()
    private val getSessionHistory: GetSessionHistoryUseCase = mockk()
    private val blocklistRepository: BlocklistRepository = mockk()
    private val dnsLogRepository: DnsLogRepository = mockk()
    private val clearHistoryUseCase: ClearHistoryUseCase = mockk(relaxed = true)

    private lateinit var viewModel: StatsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getSummaryStats() } returns statsFlow
        every { getPerAppStats() } returns perAppFlow
        every { getSessionHistory() } returns sessionFlow
        every { blocklistRepository.domainCount() } returns 150_000

        viewModel = StatsViewModel(
            getSummaryStats = getSummaryStats,
            getPerAppStats = getPerAppStats,
            getSessionHistory = getSessionHistory,
            blocklistRepository = blocklistRepository,
            dnsLogRepository = dnsLogRepository,
            clearHistoryUseCase = clearHistoryUseCase
        )
    }

    @After
    fun teardown() = Dispatchers.resetMain()

    @Test
    fun `initial state is empty`() {
        val state = viewModel.uiState.value
        assertEquals(0, state.totalQueries)
        assertEquals(0, state.trackerQueries)
        assertEquals(0, state.trackerPercentage)
        assertEquals(emptyList<DomainStatUiModel>(), state.topDomains)
        assertEquals(emptyList<AppStatUiModel>(), state.apps)
        assertEquals(emptyList<SessionUiModel>(), state.sessions)
    }

    @Test
    fun `blocklist count comes from repository`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(150_000, state.blocklistCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `stats are mapped from SummaryStats`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            statsFlow.value = SummaryStats(
                totalQueries = 200,
                trackerQueries = 50,
                topDomains = listOf(DomainStat("ads.com", 30, true)),
            )
            val state = awaitItem()
            assertEquals(200, state.totalQueries)
            assertEquals(50, state.trackerQueries)
            assertEquals(25, state.trackerPercentage)
            assertEquals(1, state.topDomains.size)
            assertEquals("ads.com", state.topDomains[0].domain)
            assertEquals(30, state.topDomains[0].count)
            assertEquals(true, state.topDomains[0].isTracker)
        }
    }

    @Test
    fun `tracker percentage rounds down`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            statsFlow.value = SummaryStats(totalQueries = 3, trackerQueries = 1, topDomains = emptyList())
            val state = awaitItem()
            assertEquals(33, state.trackerPercentage)
        }
    }

    @Test
    fun `apps and sessions are mapped correctly`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            perAppFlow.value = listOf(
                PerAppStat(
                    appInfo = AppInfo(uid = 10001, appName = "Camera", packageName = "com.android.camera"),
                    totalQueries = 10,
                    trackerQueries = 2
                )
            )
            val stateWithApp = awaitItem()
            assertEquals(1, stateWithApp.apps.size)
            assertEquals("Camera", stateWithApp.apps[0].appName)
            assertEquals("com.android.camera", stateWithApp.apps[0].packageName)

            sessionFlow.value = listOf(
                SessionSummary(
                    sessionId = 1L,
                    startedAt = 1000L,
                    endedAt = 2000L,
                    totalQueries = 10,
                    trackerQueries = 2
                )
            )
            val stateWithSession = awaitItem()
            assertEquals(1, stateWithSession.sessions.size)
            assertEquals(1L, stateWithSession.sessions[0].sessionId)
        }
    }

    @Test
    fun `clear history invokes clearHistoryUseCase`() = runTest {
        viewModel.onClearHistory()
        coVerify(exactly = 1) { clearHistoryUseCase() }
    }
}
