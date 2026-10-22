package com.masum.lantern.feature.history

import app.cash.turbine.test
import com.masum.lantern.domain.model.SessionSummary
import com.masum.lantern.domain.usecase.ClearHistoryUseCase
import com.masum.lantern.domain.usecase.GetSessionHistoryUseCase
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

class HistoryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val sessionsFlow = MutableStateFlow<List<SessionSummary>>(emptyList())

    private val getSessionHistory: GetSessionHistoryUseCase = mockk()
    private val clearHistory: ClearHistoryUseCase = mockk(relaxed = true)

    private lateinit var viewModel: HistoryViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getSessionHistory() } returns sessionsFlow
        viewModel = HistoryViewModel(getSessionHistory, clearHistory)
    }

    @After
    fun teardown() = Dispatchers.resetMain()

    private fun session(
        id: Long = 1L,
        startedAt: Long = System.currentTimeMillis() - 60_000L,
        endedAt: Long? = System.currentTimeMillis(),
        total: Int = 10,
        trackers: Int = 3,
    ) = SessionSummary(
        sessionId = id,
        startedAt = startedAt,
        endedAt = endedAt,
        totalQueries = total,
        trackerQueries = trackers,
    )

    @Test
    fun `initial state has empty sessions`() {
        assertTrue(viewModel.uiState.value.sessions.isEmpty())
    }

    @Test
    fun `sessions are mapped from domain model`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            sessionsFlow.value = listOf(session(id = 42L, total = 10, trackers = 3))
            val state = awaitItem()
            assertEquals(1, state.sessions.size)
            assertEquals(42L, state.sessions[0].sessionId)
            assertEquals(10, state.sessions[0].totalQueries)
            assertEquals(3, state.sessions[0].trackerQueries)
            assertEquals(30, state.sessions[0].trackerPercentage)
        }
    }

    @Test
    fun `active session has isActive true and duration label`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            sessionsFlow.value = listOf(session(endedAt = null))
            val state = awaitItem()
            assertTrue(state.sessions[0].isActive)
            assertEquals("Active", state.sessions[0].duration)
        }
    }

    @Test
    fun `completed session has isActive false`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            sessionsFlow.value = listOf(session(endedAt = System.currentTimeMillis()))
            val state = awaitItem()
            assertFalse(state.sessions[0].isActive)
        }
    }

    @Test
    fun `onClearHistory delegates to use case`() = runTest {
        viewModel.onClearHistory()
        coVerify { clearHistory() }
    }
}
