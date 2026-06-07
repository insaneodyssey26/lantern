package com.masum.vigil.feature.stats

import app.cash.turbine.test
import com.masum.vigil.domain.model.DomainStat
import com.masum.vigil.domain.model.SummaryStats
import com.masum.vigil.domain.repository.BlocklistRepository
import com.masum.vigil.domain.usecase.GetSummaryStatsUseCase
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

    private val getSummaryStats: GetSummaryStatsUseCase = mockk()
    private val blocklistRepository: BlocklistRepository = mockk()

    private lateinit var viewModel: StatsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getSummaryStats() } returns statsFlow
        every { blocklistRepository.domainCount() } returns 150_000
        viewModel = StatsViewModel(getSummaryStats, blocklistRepository)
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
}
