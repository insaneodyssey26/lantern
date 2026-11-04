package com.masum.lantern.data.blocklist

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlocklistDownloader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    data class DownloadResult(
        val isModified: Boolean,
        val etag: String?,
        val lastModified: String?,
        val file: File
    )

    fun downloadToFile(
        id: String,
        url: String,
        savedEtag: String?,
        savedLastModified: String?
    ): DownloadResult {
        val requestBuilder = Request.Builder().url(url)
        if (!savedEtag.isNullOrBlank()) {
            requestBuilder.header("If-None-Match", savedEtag)
        }
        if (!savedLastModified.isNullOrBlank()) {
            requestBuilder.header("If-Modified-Since", savedLastModified)
        }

        val request = requestBuilder.build()
        val response = client.newCall(request).execute()

        val targetDir = File(context.filesDir, "blocklists")
        if (!targetDir.exists()) targetDir.mkdirs()
        val targetFile = File(targetDir, "$id.txt")

        if (response.code == 304 && targetFile.exists() && targetFile.length() > 0) {
            response.close()
            return DownloadResult(
                isModified = false,
                etag = savedEtag,
                lastModified = savedLastModified,
                file = targetFile
            )
        }

        if (!response.isSuccessful) {
            val code = response.code
            response.close()
            throw IllegalStateException("HTTP failure code: $code")
        }

        val body = response.body
        val tempFile = File(targetDir, "${id}_temp.tmp")

        body.byteStream().use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }

        if (tempFile.length() < 64) {
            tempFile.delete()
            throw IllegalStateException("Downloaded blocklist is empty or corrupted")
        }

        if (targetFile.exists()) targetFile.delete()
        tempFile.renameTo(targetFile)

        val newEtag = response.header("ETag")
        val newLastModified = response.header("Last-Modified")

        return DownloadResult(
            isModified = true,
            etag = newEtag ?: savedEtag,
            lastModified = newLastModified ?: savedLastModified,
            file = targetFile
        )
    }

    fun getLocalFile(id: String): File {
        val targetDir = File(context.filesDir, "blocklists")
        return File(targetDir, "$id.txt")
    }

    fun deleteLocalFile(id: String) {
        val file = getLocalFile(id)
        if (file.exists()) file.delete()
    }
}
