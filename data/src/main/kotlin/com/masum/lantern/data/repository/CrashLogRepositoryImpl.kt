package com.masum.lantern.data.repository

import android.content.Context
import com.masum.lantern.data.crash.CrashHandler
import com.masum.lantern.domain.model.CrashLog
import com.masum.lantern.domain.repository.CrashLogRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrashLogRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : CrashLogRepository {

    private val crashFile: File
        get() = File(context.filesDir, CrashHandler.CRASH_FILE_NAME)

    private val _crashLogFlow = MutableStateFlow(readCrashLog())

    override fun getCrashLog(): Flow<CrashLog?> = _crashLogFlow.asStateFlow()

    override fun hasCrashLog(): Boolean = crashFile.exists() && crashFile.length() > 0

    override fun clearCrashLog() {
        if (crashFile.exists()) {
            crashFile.delete()
        }
        _crashLogFlow.value = null
    }

    private fun readCrashLog(): CrashLog? {
        val file = crashFile
        if (!file.exists() || file.length() == 0L) return null
        return try {
            val text = file.readText()
            val separator = "--- STACKTRACE ---\n"
            val parts = text.split(separator)
            val metadataPart = parts[0]
            val stackTrace = if (parts.size > 1) parts[1] else ""

            val map = mutableMapOf<String, String>()
            metadataPart.lines().forEach { line ->
                val eqIdx = line.indexOf('=')
                if (eqIdx > 0) {
                    val key = line.substring(0, eqIdx).trim()
                    val value = line.substring(eqIdx + 1).trim()
                    map[key] = value
                }
            }

            CrashLog(
                timestamp = map["TIMESTAMP"]?.toLongOrNull() ?: 0L,
                formattedTime = map["FORMATTED_TIME"] ?: "Unknown",
                appVersion = map["APP_VERSION"] ?: "Unknown",
                deviceModel = map["DEVICE_MODEL"] ?: "Unknown",
                androidVersion = map["ANDROID_VERSION"] ?: "Unknown",
                exceptionClass = map["EXCEPTION_CLASS"] ?: "Unknown",
                message = map["MESSAGE"] ?: "No message",
                stackTrace = stackTrace
            )
        } catch (_: Exception) {
            null
        }
    }
}
