package com.masum.lantern.feature.domainrules

import app.cash.turbine.test
import com.masum.lantern.domain.repository.SettingsRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DomainRulesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)

    private val allowedFlow = MutableStateFlow<Set<String>>(emptySet())
    private val blockedFlow = MutableStateFlow<Set<String>>(emptySet())

    private lateinit var viewModel: DomainRulesViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { settingsRepository.getUserAllowedDomains() } returns allowedFlow
        every { settingsRepository.getUserBlockedDomains() } returns blockedFlow
        viewModel = DomainRulesViewModel(settingsRepository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty lists and allowed tab selected`() {
        val state = viewModel.uiState.value
        assertTrue(state.allowedDomains.isEmpty())
        assertTrue(state.blockedDomains.isEmpty())
        assertEquals("", state.searchQuery)
        assertEquals(DomainRuleTab.ALLOWED, state.selectedTab)
    }

    @Test
    fun `domain lists are sorted alphabetically`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            allowedFlow.value = setOf("zebra.com", "apple.com", "mango.com")
            val state = awaitItem()
            assertEquals(listOf("apple.com", "mango.com", "zebra.com"), state.allowedDomains)
        }
    }

    @Test
    fun `search query filters domains`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            blockedFlow.value = setOf("doubleclick.net", "facebook.com", "adservice.google.com")
            awaitItem()

            viewModel.setSearchQuery("ad")
            val state = awaitItem()
            assertEquals(listOf("adservice.google.com"), state.blockedDomains)
        }
    }

    @Test
    fun `tab selection updates selectedTab`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            viewModel.setSelectedTab(DomainRuleTab.BLOCKED)
            val state = awaitItem()
            assertEquals(DomainRuleTab.BLOCKED, state.selectedTab)
        }
    }

    @Test
    fun `addAllowedDomain normalizes domain and updates repository`() = runTest {
        viewModel.addAllowedDomain("  EXAMPLE.COM  ")
        coVerify { settingsRepository.setUserDomainAllowed("example.com", true) }
    }

    @Test
    fun `addAllowedDomain ignores blank domain`() = runTest {
        viewModel.addAllowedDomain("   ")
        coVerify(exactly = 0) { settingsRepository.setUserDomainAllowed(any(), any()) }
    }

    @Test
    fun `removeAllowedDomain calls repository with false`() = runTest {
        viewModel.removeAllowedDomain("example.com")
        coVerify { settingsRepository.setUserDomainAllowed("example.com", false) }
    }

    @Test
    fun `addBlockedDomain normalizes domain and updates repository`() = runTest {
        viewModel.addBlockedDomain("  ADS.TRACKER.COM  ")
        coVerify { settingsRepository.setUserDomainBlocked("ads.tracker.com", true) }
    }

    @Test
    fun `removeBlockedDomain calls repository with false`() = runTest {
        viewModel.removeBlockedDomain("ads.tracker.com")
        coVerify { settingsRepository.setUserDomainBlocked("ads.tracker.com", false) }
    }
}
