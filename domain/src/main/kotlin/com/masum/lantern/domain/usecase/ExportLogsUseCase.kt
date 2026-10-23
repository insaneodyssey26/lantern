package com.masum.lantern.domain.usecase

import com.masum.lantern.domain.model.DnsQuery
import com.masum.lantern.domain.model.Verdict
import com.masum.lantern.domain.repository.DnsLogRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ExportLogsUseCase @Inject constructor(
    private val repository: DnsLogRepository,
) {

    suspend fun exportToCsv(sessionId: Long? = null): String {
        val queries = if (sessionId != null) {
            repository.getQueriesBySession(sessionId)
        } else {
            repository.getAllQueries()
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
        val sb = StringBuilder()
        sb.append("id,sessionId,timestamp,isoTime,domain,verdict,verdictCategory,isBlocked,appName,packageName\n")

        for (q in queries) {
            val isoTime = dateFormat.format(Date(q.timestamp))
            val verdictStr = when (q.verdict) {
                is Verdict.Clean -> "CLEAN"
                is Verdict.Tracker -> "TRACKER"
                is Verdict.Unknown -> "UNKNOWN"
            }
            val category = when (val v = q.verdict) {
                is Verdict.Tracker -> v.category.escapeCsv()
                else -> ""
            }

            sb.append(q.id).append(',')
            sb.append(q.sessionId).append(',')
            sb.append(q.timestamp).append(',')
            sb.append(isoTime).append(',')
            sb.append(q.domain.escapeCsv()).append(',')
            sb.append(verdictStr).append(',')
            sb.append(category).append(',')
            sb.append(q.isBlocked).append(',')
            sb.append(q.appInfo.appName.escapeCsv()).append(',')
            sb.append(q.appInfo.packageName.escapeCsv()).append('\n')
        }

        return sb.toString()
    }

    suspend fun exportToJson(sessionId: Long? = null): String {
        val queries = if (sessionId != null) {
            repository.getQueriesBySession(sessionId)
        } else {
            repository.getAllQueries()
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        val sb = StringBuilder()
        sb.append("[\n")
        queries.forEachIndexed { index, q ->
            val isoTime = dateFormat.format(Date(q.timestamp))
            val verdictStr = when (q.verdict) {
                is Verdict.Clean -> "CLEAN"
                is Verdict.Tracker -> "TRACKER"
                is Verdict.Unknown -> "UNKNOWN"
            }
            val category = when (val v = q.verdict) {
                is Verdict.Tracker -> v.category
                else -> ""
            }

            sb.append("  {\n")
            sb.append("    \"id\": ").append(q.id).append(",\n")
            sb.append("    \"sessionId\": ").append(q.sessionId).append(",\n")
            sb.append("    \"timestamp\": ").append(q.timestamp).append(",\n")
            sb.append("    \"isoTime\": \"").append(isoTime).append("\",\n")
            sb.append("    \"domain\": \"").append(q.domain.escapeJson()).append("\",\n")
            sb.append("    \"verdict\": \"").append(verdictStr).append("\",\n")
            sb.append("    \"verdictCategory\": \"").append(category.escapeJson()).append("\",\n")
            sb.append("    \"isBlocked\": ").append(q.isBlocked).append(",\n")
            sb.append("    \"appName\": \"").append(q.appInfo.appName.escapeJson()).append("\",\n")
            sb.append("    \"packageName\": \"").append(q.appInfo.packageName.escapeJson()).append("\"\n")
            sb.append("  }")
            if (index < queries.lastIndex) sb.append(",")
            sb.append("\n")
        }
        sb.append("]")
        return sb.toString()
    }

    private fun String.escapeCsv(): String {
        return if (contains(",") || contains("\"") || contains("\n")) {
            "\"" + replace("\"", "\"\"") + "\""
        } else {
            this
        }
    }

    private fun String.escapeJson(): String {
        return replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}
