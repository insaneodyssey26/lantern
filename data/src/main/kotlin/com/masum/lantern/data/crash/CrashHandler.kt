package com.masum.lantern.data.crash

import android.content.Context
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrashHandler(
    private val context: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            saveCrashReport(throwable)
        } catch (_: Exception) {
        } finally {
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun saveCrashReport(throwable: Throwable) {
        val crashFile = File(context.filesDir, CRASH_FILE_NAME)
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        val stackTrace = sw.toString()

        val versionName = try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "unknown"
        } catch (_: Exception) {
            "unknown"
        }

        val versionCode = try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            PackageInfoCompat.getLongVersionCode(pInfo).toString()
        } catch (_: Exception) {
            "0"
        }

        val now = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(now))

        val content = buildString {
            appendLine("TIMESTAMP=$now")
            appendLine("FORMATTED_TIME=$formattedDate")
            appendLine("APP_VERSION=$versionName ($versionCode)")
            appendLine("DEVICE_MODEL=${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("ANDROID_VERSION=Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            appendLine("EXCEPTION_CLASS=${throwable.javaClass.name}")
            appendLine("MESSAGE=${throwable.message ?: "No message"}")
            appendLine("--- STACKTRACE ---")
            append(stackTrace)
        }

        crashFile.writeText(content)
    }

    companion object {
        const val CRASH_FILE_NAME = "lantern_crash_report.txt"

        fun install(context: Context) {
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            if (defaultHandler !is CrashHandler) {
                Thread.setDefaultUncaughtExceptionHandler(CrashHandler(context.applicationContext, defaultHandler))
            }
        }
    }
}
