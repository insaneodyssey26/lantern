package com.masum.lantern.feature.settings

import app.cash.turbine.test
import com.masum.lantern.domain.repository.BlocklistRepository
import com.masum.lantern.domain.repository.DnsLogRepository
import com.masum.lantern.domain.repository.SettingsRepository
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

class SettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val dnsLogRepository: DnsLogRepository = mockk(relaxed = true)
    private val blocklistRepository: BlocklistRepository = mockk(relaxed = true)

    private val themeModeFlow = MutableStateFlow("dark")
    private val colorThemeFlow = MutableStateFlow("amber")
    private val pureBlackFlow = MutableStateFlow(false)
    private val useSystemFontFlow = MutableStateFlow(false)
    private val upstreamDnsFlow = MutableStateFlow("1.1.1.1")
    private val dnsProtocolFlow = MutableStateFlow("PLAIN")
    private val maxAgeDaysFlow = MutableStateFlow(30)
    private val maxRowsFlow = MutableStateFlow(50_000)
    private val totalCountFlow = MutableStateFlow(1234)

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { settingsRepository.getThemeMode() } returns themeModeFlow
        every { settingsRepository.getColorTheme() } returns colorThemeFlow
        every { settingsRepository.isPureBlack() } returns pureBlackFlow
        every { settingsRepository.isUseSystemFont() } returns useSystemFontFlow
        every { settingsRepository.getUpstreamDns() } returns upstreamDnsFlow
        every { settingsRepository.getDnsProtocol() } returns dnsProtocolFlow
        every { settingsRepository.getMaxAgeDays() } returns maxAgeDaysFlow
        every { settingsRepository.getMaxRows() } returns maxRowsFlow
        every { settingsRepository.getBypassedApps() } returns MutableStateFlow(setOf("com.bank.app"))
        every { settingsRepository.getUserAllowedDomains() } returns MutableStateFlow(setOf("allowed.com"))
        every { settingsRepository.getUserBlockedDomains() } returns MutableStateFlow(setOf("blocked.com"))

        every { dnsLogRepository.observeTotalCount() } returns totalCountFlow
        every { blocklistRepository.domainCount() } returns 85000
        every { blocklistRepository.getLastUpdatedTimestamp() } returns 1700000000L

        viewModel = SettingsViewModel(settingsRepository, dnsLogRepository, blocklistRepository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state reflects repository preferences`() = runTest {
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals("dark", state.themeMode)
            assertEquals("amber", state.colorTheme)
            assertEquals("1.1.1.1", state.upstreamDns)
            assertEquals("PLAIN", state.dnsProtocol)
            assertEquals(30, state.maxAgeDays)
            assertEquals(50_000, state.maxRows)
            assertEquals(85000, state.blocklistCount)
            assertEquals(1, state.bypassedAppsCount)
            assertEquals(2, state.domainRulesCount)
            assertFalse(state.isClearingLogs)
            assertFalse(state.isUpdatingBlocklist)
        }
    }

    @Test
    fun `preference update methods invoke settingsRepository`() = runTest {
        viewModel.setThemeMode("light")
        coVerify { settingsRepository.setThemeMode("light") }

        viewModel.setColorTheme("sage")
        coVerify { settingsRepository.setColorTheme("sage") }

        viewModel.setUpstreamDns("9.9.9.9")
        coVerify { settingsRepository.setUpstreamDns("9.9.9.9") }

        viewModel.setDnsProtocol("DOH")
        coVerify { settingsRepository.setDnsProtocol("DOH") }

        viewModel.setMaxAgeDays(7)
        coVerify { settingsRepository.setMaxAgeDays(7) }

        viewModel.setMaxRows(10000)
        coVerify { settingsRepository.setMaxRows(10000) }

        viewModel.setPureBlack(true)
        coVerify { settingsRepository.setPureBlack(true) }

        viewModel.setUseSystemFont(true)
        coVerify { settingsRepository.setUseSystemFont(true) }
    }

    @Test
    fun `clearAllLogs invokes repository clearHistory`() = runTest {
        viewModel.clearAllLogs()
        coVerify { dnsLogRepository.clearHistory() }
    }

    @Test
    fun `updateBlocklist succeeds and updates state message`() = runTest {
        coEvery { blocklistRepository.updateBlocklist() } returns Result.success(true)

        viewModel.uiState.test {
            awaitItem()
            viewModel.updateBlocklist()
            val state = expectMostRecentItem()
            assertFalse(state.isUpdatingBlocklist)
            assertEquals("Successfully updated threat database to latest rules", state.blocklistUpdateMessage)
        }
    }

    @Test
    fun `updateBlocklist reports already up to date when no changes`() = runTest {
        coEvery { blocklistRepository.updateBlocklist() } returns Result.success(false)

        viewModel.uiState.test {
            awaitItem()
            viewModel.updateBlocklist()
            val state = expectMostRecentItem()
            assertEquals("Database is already up to date", state.blocklistUpdateMessage)
        }
    }

    @Test
    fun `updateBlocklist handles failure gracefully`() = runTest {
        coEvery { blocklistRepository.updateBlocklist() } returns Result.failure(RuntimeException("Network error"))

        viewModel.uiState.test {
            awaitItem()
            viewModel.updateBlocklist()
            val state = expectMostRecentItem()
            assertEquals("Failed to update: Check internet connection", state.blocklistUpdateMessage)
        }
    }

    @Test
    fun `clearBlocklistMessage clears the feedback message`() = runTest {
        coEvery { blocklistRepository.updateBlocklist() } returns Result.success(true)

        viewModel.uiState.test {
            awaitItem()
            viewModel.updateBlocklist()
            viewModel.clearBlocklistMessage()
            val state = expectMostRecentItem()
            assertEquals(null, state.blocklistUpdateMessage)
        }
    }
}
