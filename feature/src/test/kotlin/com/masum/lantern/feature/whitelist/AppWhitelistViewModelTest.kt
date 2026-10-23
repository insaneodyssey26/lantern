package com.masum.lantern.feature.whitelist

import app.cash.turbine.test
import com.masum.lantern.domain.model.AppInfo
import com.masum.lantern.domain.repository.AppInfoRepository
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

class AppWhitelistViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val appInfoRepository: AppInfoRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)

    private val bypassedFlow = MutableStateFlow<Set<String>>(emptySet())

    private val mockApps = listOf(
        AppInfo(appName = "Bank App", packageName = "com.bank.secure", uid = 10001),
        AppInfo(appName = "Browser", packageName = "com.browser.app", uid = 10002)
    )

    private lateinit var viewModel: AppWhitelistViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { appInfoRepository.getInstalledApps() } returns mockApps
        every { settingsRepository.getBypassedApps() } returns bypassedFlow
        viewModel = AppWhitelistViewModel(appInfoRepository, settingsRepository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads installed apps and marks bypassed state correctly`() = runTest {
        viewModel.uiState.test {
            bypassedFlow.value = setOf("com.bank.secure")
            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
            assertEquals(2, state.apps.size)
            assertTrue(state.apps.first { it.appInfo.packageName == "com.bank.secure" }.isBypassed)
            assertFalse(state.apps.first { it.appInfo.packageName == "com.browser.app" }.isBypassed)
        }
    }

    @Test
    fun `toggleAppBypass delegates to repository`() = runTest {
        viewModel.toggleAppBypass("com.bank.secure", true)
        coVerify { settingsRepository.setAppBypassed("com.bank.secure", true) }
    }

    @Test
    fun `clearAllBypasses removes all bypassed apps`() = runTest {
        viewModel.uiState.test {
            bypassedFlow.value = setOf("com.bank.secure", "com.browser.app")
            expectMostRecentItem()

            viewModel.clearAllBypasses()
            coVerify { settingsRepository.setAppBypassed("com.bank.secure", false) }
            coVerify { settingsRepository.setAppBypassed("com.browser.app", false) }
        }
    }
}
