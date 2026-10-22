package com.masum.lantern.data.blocklist

import android.content.Context
import com.masum.lantern.core.common.dispatcher.DispatcherProvider
import com.masum.lantern.domain.model.Verdict
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlocklistMatcher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: DispatcherProvider,
) {
    @Volatile private var trie: BlocklistTrie? = null
    private val prefs by lazy { context.getSharedPreferences("lantern_blocklist", Context.MODE_PRIVATE) }

    suspend fun load() = withContext(dispatchers.io) {
        val downloadedFile = getDownloadedBlocklistFile()
        val inputStream = if (downloadedFile.exists() && downloadedFile.length() > 0) {
            FileInputStream(downloadedFile)
        } else {
            context.assets.open("blocklist/hosts.txt")
        }
        val loaded = inputStream.use { BlocklistLoader.load(it) }
        trie = loaded
    }

    suspend fun updateFromRemote(): Result<Boolean> = withContext(dispatchers.io) {
        runCatching {
            val url = URL("https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 30_000
                requestMethod = "GET"
                val savedEtag = prefs.getString("blocklist_etag", null)
                if (savedEtag != null) {
                    setRequestProperty("If-None-Match", savedEtag)
                }
            }

            connection.connect()
            val responseCode = connection.responseCode

            if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
                prefs.edit().putLong("last_checked_timestamp", System.currentTimeMillis()).apply()
                return@runCatching false
            }

            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw IllegalStateException("HTTP error $responseCode")
            }

            val etag = connection.getHeaderField("ETag")
            val targetFile = getDownloadedBlocklistFile()
            val tempFile = File(context.filesDir, "hosts_temp.txt")

            connection.inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            if (tempFile.length() < 100_000) {
                tempFile.delete()
                throw IllegalStateException("Downloaded file suspiciously small")
            }

            if (targetFile.exists()) targetFile.delete()
            tempFile.renameTo(targetFile)

            val editor = prefs.edit()
            if (etag != null) editor.putString("blocklist_etag", etag)
            editor.putLong("last_updated_timestamp", System.currentTimeMillis())
            editor.putLong("last_checked_timestamp", System.currentTimeMillis())
            editor.apply()

            load()
            true
        }
    }

    fun getLastUpdatedTimestamp(): Long {
        val ts = prefs.getLong("last_updated_timestamp", 0L)
        if (ts > 0) return ts
        return getDownloadedBlocklistFile().takeIf { it.exists() }?.lastModified() ?: 0L
    }

    private fun getDownloadedBlocklistFile(): File {
        val dir = File(context.filesDir, "blocklist")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "hosts.txt")
    }

    fun classify(domain: String): Verdict {
        val t = trie ?: return Verdict.Unknown
        return if (t.matches(domain)) Verdict.Tracker() else Verdict.Clean
    }

    fun domainCount(): Int = trie?.size ?: 0

    fun isReady(): Boolean = trie != null
}
