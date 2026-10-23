package com.masum.lantern.domain.usecase

import com.masum.lantern.domain.model.AppInfo
import com.masum.lantern.domain.model.DnsQuery
import com.masum.lantern.domain.model.Verdict
import com.masum.lantern.domain.repository.DnsLogRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExportLogsUseCaseTest {

    private val repository: DnsLogRepository = mockk()
    private lateinit var useCase: ExportLogsUseCase

    private val sampleQueries = listOf(
        DnsQuery(
            id = 1L,
            sessionId = 100L,
            domain = "tracker,analytics.com",
            timestamp = 1700000000000L,
            verdict = Verdict.Tracker(category = "Ad \"Analytics\""),
            isBlocked = true,
            appInfo = AppInfo(appName = "Browser", packageName = "com.browser", uid = 1001)
        ),
        DnsQuery(
            id = 2L,
            sessionId = 100L,
            domain = "google.com",
            timestamp = 1700000001000L,
            verdict = Verdict.Clean,
            isBlocked = false,
            appInfo = AppInfo(appName = "Search", packageName = "com.search", uid = 1002)
        )
    )

    @Before
    fun setup() {
        useCase = ExportLogsUseCase(repository)
    }

    @Test
    fun `exportToCsv outputs header and properly escapes values`() = runTest {
        coEvery { repository.getAllQueries() } returns sampleQueries

        val csv = useCase.exportToCsv()
        assertTrue(csv.startsWith("id,sessionId,timestamp,isoTime,domain,verdict,verdictCategory,isBlocked,appName,packageName\n"))
        assertTrue(csv.contains("\"tracker,analytics.com\""))
        assertTrue(csv.contains("\"Ad \"\"Analytics\"\"\""))
        assertTrue(csv.contains("CLEAN"))
        assertTrue(csv.contains("google.com"))
    }

    @Test
    fun `exportToCsv with sessionId calls getQueriesBySession`() = runTest {
        coEvery { repository.getQueriesBySession(100L) } returns sampleQueries

        val csv = useCase.exportToCsv(100L)
        assertTrue(csv.contains("100"))
        assertTrue(csv.contains("com.browser"))
    }

    @Test
    fun `exportToJson outputs valid json array`() = runTest {
        coEvery { repository.getAllQueries() } returns sampleQueries

        val json = useCase.exportToJson()
        assertTrue(json.startsWith("[\n"))
        assertTrue(json.endsWith("\n]"))
        assertTrue(json.contains("\"domain\": \"tracker,analytics.com\""))
        assertTrue(json.contains("\"verdict\": \"TRACKER\""))
        assertTrue(json.contains("\"verdictCategory\": \"Ad \\\"Analytics\\\"\""))
        assertTrue(json.contains("\"isBlocked\": true"))
    }
}
