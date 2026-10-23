package com.masum.lantern.feature.stats.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.masum.lantern.domain.model.AppInfo
import com.masum.lantern.domain.model.DnsQuery
import com.masum.lantern.domain.model.PerAppStat
import com.masum.lantern.domain.model.SessionSummary
import com.masum.lantern.domain.model.Verdict
import com.masum.lantern.domain.repository.DnsLogRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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

class SessionDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val dnsLogRepository: DnsLogRepository = mockk()

    private val sessionFlow = MutableStateFlow<SessionSummary?>(null)
    private val perAppFlow = MutableStateFlow<List<PerAppStat>>(emptyList())
    private val queriesFlow = MutableStateFlow<List<DnsQuery>>(emptyList())

    private val sampleSession = SessionSummary(
        sessionId = 42L,
        startedAt = 1000L,
        endedAt = 61000L,
        totalQueries = 10,
        trackerQueries = 3
    )

    private val sampleApp = PerAppStat(
        appInfo = AppInfo(appName = "Browser", packageName = "com.browser", uid = 1001),
        totalQueries = 5,
        trackerQueries = 2
    )

    private val sampleQuery = DnsQuery(
        id = 1L,
        sessionId = 42L,
        domain = "analytics.google.com",
        timestamp = 2000L,
        verdict = Verdict.Tracker(category = "Analytics"),
        isBlocked = true,
        appInfo = AppInfo(appName = "Browser", packageName = "com.browser", uid = 1001)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { dnsLogRepository.observeSession(42L) } returns sessionFlow
        every { dnsLogRepository.observePerAppStats(42L) } returns perAppFlow
        every { dnsLogRepository.observeSessionQueries(42L) } returns queriesFlow
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(sessionId: Long = 42L): SessionDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("sessionId" to sessionId))
        return SessionDetailViewModel(savedStateHandle, dnsLogRepository)
    }

    @Test
    fun `initial state shows loading true`() {
        val viewModel = createViewModel()
        val state = viewModel.uiState.value
        assertEquals(42L, state.sessionId)
        assertTrue(state.isLoading)
        assertEquals(0, state.totalQueries)
    }

    @Test
    fun `emits populated session details when repository yields data`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            sessionFlow.value = sampleSession
            perAppFlow.value = listOf(sampleApp)
            queriesFlow.value = listOf(sampleQuery)

            val state = expectMostRecentItem()
            assertEquals(42L, state.sessionId)
            assertEquals(10, state.totalQueries)
            assertEquals(3, state.trackerQueries)
            assertEquals(30, state.trackerPercentage)
            assertFalse(state.isActive)
            assertEquals(1, state.apps.size)
            assertEquals("Browser", state.apps[0].appName)
            assertEquals(1, state.queries.size)
            assertEquals("analytics.google.com", state.queries[0].domain)
            assertTrue(state.queries[0].isTracker)
            assertTrue(state.queries[0].isBlocked)
        }
    }

    @Test
    fun `switching tab updates state`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()
            viewModel.onTabSelected(SessionDetailTab.APPS)
            val state = awaitItem()
            assertEquals(SessionDetailTab.APPS, state.selectedTab)
        }
    }

    @Test
    fun `getSessionAppQueries delegates to repository`() = runTest {
        val viewModel = createViewModel()
        every { dnsLogRepository.observeSessionAppQueries(42L, "com.browser") } returns flowOf(listOf(sampleQuery))

        viewModel.getSessionAppQueries("com.browser").test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("com.browser", result[0].appInfo.packageName)
            awaitComplete()
        }
    }
}
