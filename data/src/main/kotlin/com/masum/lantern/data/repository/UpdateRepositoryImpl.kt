package com.masum.lantern.data.repository

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.masum.lantern.domain.model.AppUpdate
import com.masum.lantern.domain.model.UpdateDownloadState
import com.masum.lantern.domain.repository.UpdateRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : UpdateRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    override suspend fun checkForUpdate(): Result<AppUpdate?> = withContext(Dispatchers.IO) {
        runCatching {
            val packageInfo = runCatching {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.getPackageInfo(context.packageName, 0)
                }
            }.getOrNull()

            val currentVersionName = packageInfo?.versionName?.removeSuffix("-debug") ?: "2.0"

            val req = Request.Builder()
                .url("https://api.github.com/repos/insaneodyssey26/lantern/releases/latest")
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "Lantern-Android")
                .build()

            val responseBody = client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) resp.body.string() else null
            } ?: return@runCatching null

            val root = JSONObject(responseBody)

            val tagName = root.optString("tag_name", "").trim()
            val releaseTitle = root.optString("name", tagName)
            val releaseNotes = root.optString("body", "")
            val htmlUrl = root.optString("html_url", "")
            val publishedAt = root.optString("published_at", "")

            val cleanTagVersion = tagName.removePrefix("v").removePrefix("Vigil-").removePrefix("Lantern-").trim()

            val assets = root.optJSONArray("assets") ?: return@runCatching null
            var apkDownloadUrl: String? = null
            var apkFileName = "lantern-update.apk"
            var apkSizeBytes = 0L

            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val name = asset.optString("name", "")
                if (name.endsWith(".apk", ignoreCase = true)) {
                    apkDownloadUrl = asset.optString("browser_download_url")
                    apkFileName = name
                    apkSizeBytes = asset.optLong("size", 0L)
                    break
                }
            }

            if (apkDownloadUrl == null) {
                return@runCatching null
            }

            val isNewer = isVersionNewer(cleanTagVersion, currentVersionName)

            if (!isNewer) {
                return@runCatching null
            }

            AppUpdate(
                versionName = cleanTagVersion.ifBlank { tagName },
                versionCode = 0,
                releaseTitle = releaseTitle,
                releaseNotes = releaseNotes,
                apkDownloadUrl = apkDownloadUrl,
                apkFileName = apkFileName,
                apkSizeBytes = apkSizeBytes,
                publishedAt = publishedAt,
                htmlUrl = htmlUrl
            )
        }
    }

    override fun downloadUpdate(apkUrl: String, fileName: String): Flow<UpdateDownloadState> = flow {
        emit(UpdateDownloadState.Connecting)

        val updatesDir = File(context.cacheDir, "updates")
        if (!updatesDir.exists()) {
            updatesDir.mkdirs()
        }
        val targetFile = File(updatesDir, fileName)
        if (targetFile.exists()) {
            targetFile.delete()
        }

        val request = Request.Builder()
            .url(apkUrl)
            .header("User-Agent", "Lantern-Android")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                emit(UpdateDownloadState.Error("Server responded with HTTP ${response.code}"))
                return@use
            }

            val body = response.body
            val contentLength = body.contentLength()
            var bytesDownloaded = 0L
            var lastEmitTime = System.currentTimeMillis()
            var bytesSinceLastEmit = 0L
            var currentSpeed = 0L

            val buffer = ByteArray(8192)
            body.byteStream().use { input ->
                FileOutputStream(targetFile).use { output ->
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        bytesDownloaded += read
                        bytesSinceLastEmit += read

                        val now = System.currentTimeMillis()
                        val diff = now - lastEmitTime
                        if (diff >= 200) {
                            currentSpeed = (bytesSinceLastEmit * 1000L) / diff
                            val percentage = if (contentLength > 0) {
                                (bytesDownloaded.toFloat() / contentLength.toFloat()).coerceIn(0f, 1f)
                            } else {
                                0f
                            }
                            emit(
                                UpdateDownloadState.Progress(
                                    bytesDownloaded = bytesDownloaded,
                                    totalBytes = contentLength,
                                    percentage = percentage,
                                    speedBytesPerSec = currentSpeed
                                )
                            )
                            lastEmitTime = now
                            bytesSinceLastEmit = 0L
                        }
                    }
                    output.flush()
                }
            }

            emit(UpdateDownloadState.Completed(targetFile.absolutePath))
        }
    }.flowOn(Dispatchers.IO)

    override fun installUpdate(filePath: String): Result<Unit> = runCatching {
        val file = File(filePath)
        if (!file.exists()) {
            error("Update package file does not exist at $filePath")
        }

        val authority = "${context.packageName}.fileprovider"
        val contentUri = FileProvider.getUriForFile(context, authority, file)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun isVersionNewer(remote: String, current: String): Boolean {
        val cleanRemote = remote.substringBefore('-').substringBefore('+')
        val cleanCurrent = current.substringBefore('-').substringBefore('+')

        val remoteParts = cleanRemote.split('.').mapNotNull { it.toIntOrNull() }
        val currentParts = cleanCurrent.split('.').mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }
}
