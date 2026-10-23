package com.masum.lantern.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingsRepositoryImplTest {

    private lateinit var context: Context
    private lateinit var repository: SettingsRepositoryImpl

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("vigil_settings", Context.MODE_PRIVATE).edit().clear().commit()
        repository = SettingsRepositoryImpl(context)
    }

    @Test
    fun `default values match requirements`() = runTest {
        assertEquals("system", repository.getThemeModeSync())
        assertEquals("system", repository.getColorThemeSync())
        assertEquals("UDP", repository.getDnsProtocolSync())
        assertFalse(repository.isPureBlackSync())
        assertFalse(repository.isUseSystemFontSync())
        assertFalse(repository.isOnboardingCompletedSync())
        assertFalse(repository.isFeatureGuideCompletedSync())
        assertTrue(repository.getUserAllowedDomainsSync().isEmpty())
        assertTrue(repository.getUserBlockedDomainsSync().isEmpty())
    }

    @Test
    fun `setThemeMode updates flow and sync values`() = runTest {
        repository.getThemeMode().test {
            assertEquals("system", awaitItem())
            repository.setThemeMode("dark")
            assertEquals("dark", awaitItem())
        }
        assertEquals("dark", repository.getThemeModeSync())
    }

    @Test
    fun `setColorTheme updates flow and sync values`() = runTest {
        repository.getColorTheme().test {
            assertEquals("system", awaitItem())
            repository.setColorTheme("sage")
            assertEquals("sage", awaitItem())
        }
        assertEquals("sage", repository.getColorThemeSync())
    }

    @Test
    fun `setUpstreamDns updates flow`() = runTest {
        repository.getUpstreamDns().test {
            assertEquals("1.1.1.1", awaitItem())
            repository.setUpstreamDns("9.9.9.9")
            assertEquals("9.9.9.9", awaitItem())
        }
    }

    @Test
    fun `setDnsProtocol updates flow and sync`() = runTest {
        repository.getDnsProtocol().test {
            assertEquals("UDP", awaitItem())
            repository.setDnsProtocol("DOH")
            assertEquals("DOH", awaitItem())
        }
        assertEquals("DOH", repository.getDnsProtocolSync())
    }

    @Test
    fun `setMaxAgeDays and setMaxRows update flows`() = runTest {
        repository.getMaxAgeDays().test {
            assertEquals(7, awaitItem())
            repository.setMaxAgeDays(30)
            assertEquals(30, awaitItem())
        }

        repository.getMaxRows().test {
            assertEquals(50_000, awaitItem())
            repository.setMaxRows(100_000)
            assertEquals(100_000, awaitItem())
        }
    }

    @Test
    fun `setBlockingEnabled updates flow`() = runTest {
        repository.isBlockingEnabled().test {
            assertTrue(awaitItem())
            repository.setBlockingEnabled(false)
            assertFalse(awaitItem())
        }
    }

    @Test
    fun `app bypass add and remove works`() = runTest {
        repository.getBypassedApps().test {
            assertTrue(awaitItem().isEmpty())
            repository.setAppBypassed("com.bank.app", true)
            assertEquals(setOf("com.bank.app"), awaitItem())
            repository.setAppBypassed("com.bank.app", false)
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun `setting allowed domain removes it from blocked domains if present`() = runTest {
        repository.setUserDomainBlocked("tracker.com", true)
        assertTrue(repository.getUserBlockedDomainsSync().contains("tracker.com"))

        repository.setUserDomainAllowed("tracker.com", true)
        assertTrue(repository.getUserAllowedDomainsSync().contains("tracker.com"))
        assertFalse(repository.getUserBlockedDomainsSync().contains("tracker.com"))
    }

    @Test
    fun `setting blocked domain removes it from allowed domains if present`() = runTest {
        repository.setUserDomainAllowed("allowed.com", true)
        assertTrue(repository.getUserAllowedDomainsSync().contains("allowed.com"))

        repository.setUserDomainBlocked("allowed.com", true)
        assertTrue(repository.getUserBlockedDomainsSync().contains("allowed.com"))
        assertFalse(repository.getUserAllowedDomainsSync().contains("allowed.com"))
    }

    @Test
    fun `isOnboardingCompleted updates correctly`() = runTest {
        repository.isOnboardingCompleted().test {
            assertFalse(awaitItem())
            repository.setOnboardingCompleted(true)
            assertTrue(awaitItem())
        }
        assertTrue(repository.isOnboardingCompletedSync())
    }

    @Test
    fun `isFeatureGuideCompleted updates correctly`() = runTest {
        repository.isFeatureGuideCompleted().test {
            assertFalse(awaitItem())
            repository.setFeatureGuideCompleted(true)
            assertTrue(awaitItem())
        }
        assertTrue(repository.isFeatureGuideCompletedSync())
    }
}
