package com.masum.vigil.data.mapper

import com.masum.vigil.core.database.entity.DnsQueryEntity
import com.masum.vigil.core.database.entity.DnsQueryEntity.Companion.VERDICT_CLEAN
import com.masum.vigil.core.database.entity.DnsQueryEntity.Companion.VERDICT_TRACKER
import com.masum.vigil.core.database.model.DomainStatTuple
import com.masum.vigil.core.database.model.PerAppStatTuple
import com.masum.vigil.core.database.model.SessionWithCountsTuple
import com.masum.vigil.core.database.model.SummaryTotals
import com.masum.vigil.domain.model.AppInfo
import com.masum.vigil.domain.model.DnsQuery
import com.masum.vigil.domain.model.DomainStat
import com.masum.vigil.domain.model.PerAppStat
import com.masum.vigil.domain.model.SessionSummary
import com.masum.vigil.domain.model.SummaryStats
import com.masum.vigil.domain.model.Verdict

fun DnsQuery.toEntity(): DnsQueryEntity = DnsQueryEntity(
    id = id,
    sessionId = sessionId,
    domain = domain,
    timestamp = timestamp,
    verdictType = when (verdict) {
        is Verdict.Tracker -> VERDICT_TRACKER
        Verdict.Clean -> VERDICT_CLEAN
        Verdict.Unknown -> DnsQueryEntity.VERDICT_UNKNOWN
    },
    verdictCategory = (verdict as? Verdict.Tracker)?.category ?: "",
    appUid = appInfo.uid,
    appPackageName = appInfo.packageName,
    appName = appInfo.appName,
)

fun DnsQueryEntity.toDomain(): DnsQuery = DnsQuery(
    id = id,
    sessionId = sessionId,
    domain = domain,
    timestamp = timestamp,
    verdict = when (verdictType) {
        VERDICT_TRACKER -> Verdict.Tracker(verdictCategory)
        VERDICT_CLEAN -> Verdict.Clean
        else -> Verdict.Unknown
    },
    appInfo = AppInfo(uid = appUid, packageName = appPackageName, appName = appName),
)

fun SessionWithCountsTuple.toSessionSummary(): SessionSummary = SessionSummary(
    sessionId = sessionId,
    startedAt = startedAt,
    endedAt = endedAt,
    totalQueries = totalQueries,
    trackerQueries = trackerQueries,
)

fun PerAppStatTuple.toPerAppStat(): PerAppStat = PerAppStat(
    appInfo = AppInfo(uid = appUid, packageName = appPackageName, appName = appName),
    totalQueries = totalQueries,
    trackerQueries = trackerQueries,
)

fun SummaryTotals.toSummaryStats(topDomains: List<DomainStatTuple>): SummaryStats = SummaryStats(
    totalQueries = totalQueries,
    trackerQueries = trackerQueries,
    topDomains = topDomains.map { DomainStat(domain = it.domain, count = it.count, isTracker = it.isTracker != 0) },
)
