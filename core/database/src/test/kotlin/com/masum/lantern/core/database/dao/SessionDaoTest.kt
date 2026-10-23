package com.masum.lantern.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.masum.lantern.core.database.LanternDatabase
import com.masum.lantern.core.database.entity.DnsQueryEntity
import com.masum.lantern.core.database.entity.DnsQueryEntity.Companion.VERDICT_CLEAN
import com.masum.lantern.core.database.entity.DnsQueryEntity.Companion.VERDICT_TRACKER
import com.masum.lantern.core.database.entity.SessionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class SessionDaoTest {

    private lateinit var db: LanternDatabase
    private lateinit var sessionDao: SessionDao
    private lateinit var dnsQueryDao: DnsQueryDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, LanternDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        sessionDao = db.sessionDao()
        dnsQueryDao = db.dnsQueryDao()
    }

    @After
    fun teardown() = db.close()

    @Test
    fun `insert and observeSessionWithCounts calculates total and tracker counts correctly`() = runTest {
        val sessionId = sessionDao.insert(SessionEntity(startedAt = 1000L, endedAt = null))

        dnsQueryDao.insertAll(
            listOf(
                DnsQueryEntity(sessionId = sessionId, domain = "clean1.com", timestamp = 1100L, verdictType = VERDICT_CLEAN, verdictCategory = "", appUid = 0, appPackageName = "", appName = ""),
                DnsQueryEntity(sessionId = sessionId, domain = "clean2.com", timestamp = 1200L, verdictType = VERDICT_CLEAN, verdictCategory = "", appUid = 0, appPackageName = "", appName = ""),
                DnsQueryEntity(sessionId = sessionId, domain = "tracker.com", timestamp = 1300L, verdictType = VERDICT_TRACKER, verdictCategory = "", appUid = 0, appPackageName = "", appName = "")
            )
        )

        val result = sessionDao.observeSessionWithCounts(sessionId).first()
        assertNotNull(result)
        assertEquals(sessionId, result?.sessionId)
        assertEquals(3, result?.totalQueries)
        assertEquals(1, result?.trackerQueries)
        assertNull(result?.endedAt)
    }

    @Test
    fun `updateEndTime updates session end timestamp`() = runTest {
        val sessionId = sessionDao.insert(SessionEntity(startedAt = 1000L, endedAt = null))
        sessionDao.updateEndTime(sessionId, 5000L)

        val result = sessionDao.observeSessionWithCounts(sessionId).first()
        assertEquals(5000L, result?.endedAt)
    }

    @Test
    fun `closeAllActiveSessions sets endedAt for all active sessions`() = runTest {
        val s1 = sessionDao.insert(SessionEntity(startedAt = 1000L, endedAt = null))
        val s2 = sessionDao.insert(SessionEntity(startedAt = 2000L, endedAt = null))
        val s3 = sessionDao.insert(SessionEntity(startedAt = 3000L, endedAt = 4000L))

        sessionDao.closeAllActiveSessions(9999L)

        val r1 = sessionDao.observeSessionWithCounts(s1).first()
        val r2 = sessionDao.observeSessionWithCounts(s2).first()
        val r3 = sessionDao.observeSessionWithCounts(s3).first()

        assertEquals(9999L, r1?.endedAt)
        assertEquals(9999L, r2?.endedAt)
        assertEquals(4000L, r3?.endedAt)
    }

    @Test
    fun `deleteAll removes all sessions`() = runTest {
        sessionDao.insert(SessionEntity(startedAt = 1000L, endedAt = null))
        sessionDao.deleteAll()

        val list = sessionDao.observeAll().first()
        assertEquals(0, list.size)
    }
}
