package com.masum.lantern.domain.usecase

import app.cash.turbine.test
import com.masum.lantern.domain.model.AppInfo
import com.masum.lantern.domain.model.DnsQuery
import com.masum.lantern.domain.model.PerAppStat
import com.masum.lantern.domain.model.SessionSummary
import com.masum.lantern.domain.model.SummaryStats
import com.masum.lantern.domain.model.Verdict
import com.masum.lantern.domain.repository.DnsLogRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DnsLogUseCasesTest {

    private val repository: DnsLogRepository = mockk(relaxed = true)

    @Test
    fun `ClearHistoryUseCase invokes repository clearHistory`() = runTest {
        val useCase = ClearHistoryUseCase(repository)
        useCase()
        coVerify(exactly = 1) { repository.clearHistory() }
    }

    @Test
    fun `GetSessionHistoryUseCase delegates to repository`() = runTest {
        val expected = listOf(
            SessionSummary(sessionId = 1L, startedAt = 1000L, endedAt = 2000L, totalQueries = 5, trackerQueries = 1)
        )
        every { repository.observeSessionHistory() } returns flowOf(expected)

        val useCase = GetSessionHistoryUseCase(repository)
        useCase().test {
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `GetPerAppStatsUseCase delegates to repository with optional sessionId`() = runTest {
        val expected = listOf(
            PerAppStat(
                appInfo = AppInfo(appName = "Chrome", packageName = "com.android.chrome", uid = 100),
                totalQueries = 20,
                trackerQueries = 5
            )
        )
        every { repository.observePerAppStats(42L) } returns flowOf(expected)

        val useCase = GetPerAppStatsUseCase(repository)
        useCase(42L).test {
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `GetSummaryStatsUseCase delegates to repository`() = runTest {
        val expected = SummaryStats(totalQueries = 50, trackerQueries = 10, topDomains = emptyList())
        every { repository.observeSummaryStats() } returns flowOf(expected)

        val useCase = GetSummaryStatsUseCase(repository)
        useCase().test {
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `ObserveLiveFeedUseCase delegates to repository`() = runTest {
        val query = DnsQuery(
            id = 1L,
            sessionId = 1L,
            domain = "tracker.com",
            timestamp = 1234L,
            verdict = Verdict.Tracker(),
            appInfo = AppInfo.UNKNOWN
        )
        every { repository.observeLiveFeed() } returns flowOf(query)

        val useCase = ObserveLiveFeedUseCase(repository)
        useCase().test {
            assertEquals(query, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `GetRecentQueriesUseCase delegates to repository`() = runTest {
        val expected = listOf(
            DnsQuery(
                id = 1L,
                sessionId = 1L,
                domain = "example.com",
                timestamp = 1234L,
                verdict = Verdict.Clean,
                appInfo = AppInfo.UNKNOWN
            )
        )
        coEvery { repository.getRecentQueries(50) } returns expected

        val useCase = GetRecentQueriesUseCase(repository)
        val result = useCase(50)
        assertEquals(expected, result)
        coVerify { repository.getRecentQueries(50) }
    }
}
