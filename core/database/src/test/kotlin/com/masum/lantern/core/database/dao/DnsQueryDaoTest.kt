package com.masum.lantern.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.masum.lantern.core.database.LanternDatabase
import com.masum.lantern.core.database.entity.DnsQueryEntity
import com.masum.lantern.core.database.entity.DnsQueryEntity.Companion.VERDICT_CLEAN
import com.masum.lantern.core.database.entity.DnsQueryEntity.Companion.VERDICT_TRACKER
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class DnsQueryDaoTest {

    private lateinit var db: LanternDatabase
    private lateinit var dao: DnsQueryDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, LanternDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.dnsQueryDao()
    }

    @After
    fun teardown() = db.close()

    private fun entity(
        domain: String,
        verdictType: Int = VERDICT_CLEAN,
        timestamp: Long = System.currentTimeMillis(),
        sessionId: Long = 1L,
        appPackageName: String = "com.example",
        appName: String = "Example",
        appUid: Int = 1000,
    ) = DnsQueryEntity(
        sessionId = sessionId,
        domain = domain,
        timestamp = timestamp,
        verdictType = verdictType,
        verdictCategory = "",
        appUid = appUid,
        appPackageName = appPackageName,
        appName = appName,
    )

    @Test
    fun `insert and retrieve recent queries`() = runTest {
        dao.insertAll(listOf(entity("google.com"), entity("tracker.com", VERDICT_TRACKER)))
        val result = dao.observeRecent().first()
        assertEquals(2, result.size)
    }

    @Test
    fun `observeRecent returns newest first`() = runTest {
        dao.insertAll(listOf(
            entity("first.com", timestamp = 1000L),
            entity("second.com", timestamp = 2000L),
        ))
        val result = dao.observeRecent().first()
        assertEquals("second.com", result[0].domain)
        assertEquals("first.com", result[1].domain)
    }

    @Test
    fun `summary totals counts correctly`() = runTest {
        dao.insertAll(listOf(
            entity("clean.com", VERDICT_CLEAN),
            entity("tracker.com", VERDICT_TRACKER),
            entity("tracker2.com", VERDICT_TRACKER),
        ))
        val totals = dao.observeSummaryTotals().first()
        assertEquals(3, totals.totalQueries)
        assertEquals(2, totals.trackerQueries)
    }

    @Test
    fun `per app stats groups by package`() = runTest {
        dao.insertAll(listOf(
            entity("a.com", appPackageName = "com.app1", appName = "App1", appUid = 1),
            entity("b.com", appPackageName = "com.app1", appName = "App1", appUid = 1),
            entity("c.com", appPackageName = "com.app2", appName = "App2", appUid = 2),
        ))
        val stats = dao.observePerAppStats(null).first()
        assertEquals(2, stats.size)
        val app1 = stats.first { it.appPackageName == "com.app1" }
        assertEquals(2, app1.totalQueries)
    }

    @Test
    fun `per app stats filters by session`() = runTest {
        dao.insertAll(listOf(
            entity("a.com", sessionId = 1L),
            entity("b.com", sessionId = 2L),
        ))
        val stats = dao.observePerAppStats(1L).first()
        assertEquals(1, stats.sumOf { it.totalQueries })
    }

    @Test
    fun `deleteOlderThan removes only old rows`() = runTest {
        dao.insertAll(listOf(
            entity("old.com", timestamp = 1000L),
            entity("new.com", timestamp = 9000L),
        ))
        dao.deleteOlderThan(cutoff = 5000L)
        assertEquals(1, dao.count())
    }

    @Test
    fun `trimToMaxRows keeps only newest rows`() = runTest {
        dao.insertAll((1..10).map { entity("domain$it.com", timestamp = it.toLong()) })
        dao.trimToMaxRows(maxRows = 3)
        assertEquals(3, dao.count())
    }

    @Test
    fun `deleteAll clears table`() = runTest {
        dao.insertAll(listOf(entity("a.com"), entity("b.com")))
        dao.deleteAll()
        assertEquals(0, dao.count())
    }

    @Test
    fun `top domains returns correct counts`() = runTest {
        dao.insertAll(listOf(
            entity("ads.com", VERDICT_TRACKER),
            entity("ads.com", VERDICT_TRACKER),
            entity("clean.com", VERDICT_CLEAN),
        ))
        val top = dao.observeTopDomains().first()
        assertEquals("ads.com", top[0].domain)
        assertEquals(2, top[0].count)
        assertEquals(1, top[0].isTracker)
        assertEquals(0, top[1].isTracker)
    }
}
