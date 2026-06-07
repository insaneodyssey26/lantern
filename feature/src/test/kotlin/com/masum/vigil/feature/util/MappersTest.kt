package com.masum.vigil.feature.util

import com.masum.vigil.core.ui.components.VerdictUiState
import com.masum.vigil.domain.model.Verdict
import org.junit.Assert.assertEquals
import org.junit.Test

class MappersTest {

    @Test
    fun `Verdict Tracker maps to VerdictUiState Tracker`() {
        assertEquals(VerdictUiState.Tracker, Verdict.Tracker("ads").toUiState())
    }

    @Test
    fun `Verdict Clean maps to VerdictUiState Clean`() {
        assertEquals(VerdictUiState.Clean, Verdict.Clean.toUiState())
    }

    @Test
    fun `Verdict Unknown maps to VerdictUiState Unknown`() {
        assertEquals(VerdictUiState.Unknown, Verdict.Unknown.toUiState())
    }

    @Test
    fun `formatDuration returns hours and minutes for long sessions`() {
        val start = 0L
        val end = 90 * 60 * 1000L
        assertEquals("1h 30m", formatDuration(start, end))
    }

    @Test
    fun `formatDuration returns minutes for sessions under one hour`() {
        val start = 0L
        val end = 45 * 60 * 1000L
        assertEquals("45m", formatDuration(start, end))
    }

    @Test
    fun `formatDuration returns less than 1 minute for short sessions`() {
        val start = 0L
        val end = 30_000L
        assertEquals("<1m", formatDuration(start, end))
    }

    @Test
    fun `formatDuration returns zero minutes marker for exactly 0 ms`() {
        assertEquals("<1m", formatDuration(100L, 100L))
    }
}
