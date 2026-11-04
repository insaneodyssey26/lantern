package com.masum.lantern.data.blocklist

import android.content.Context
import com.masum.lantern.core.common.dispatcher.DispatcherProvider
import com.masum.lantern.core.database.dao.BlocklistDao
import com.masum.lantern.core.database.entity.BlocklistEntity
import com.masum.lantern.domain.model.Verdict
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.util.zip.GZIPInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlocklistMatcher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val blocklistDao: BlocklistDao,
    private val downloader: BlocklistDownloader,
    private val dispatchers: DispatcherProvider,
) {
    @Volatile private var trie: BlocklistTrie? = null
    private val prefs by lazy { context.getSharedPreferences("lantern_blocklist", Context.MODE_PRIVATE) }

    suspend fun load() = withContext(dispatchers.io) {
        val enabledEntities = blocklistDao.getEnabled()
        val newTrie = BlocklistTrie()

        var hasLoadedAny = false
        for (entity in enabledEntities) {
            val localFile = downloader.getLocalFile(entity.id)
            if (localFile.exists() && localFile.length() > 0) {
                FileInputStream(localFile).use { stream ->
                    BlocklistLoader.loadInto(stream, newTrie)
                }
                hasLoadedAny = true
            }
        }

        if (!hasLoadedAny) {
            val fallbackFile = getLegacyBlocklistFile()
            val inputStream = if (fallbackFile.exists() && fallbackFile.length() > 0) {
                FileInputStream(fallbackFile)
            } else {
                val assetNames = context.assets.list("blocklist") ?: emptyArray()
                if ("hosts.txt.gz" in assetNames) {
                    GZIPInputStream(context.assets.open("blocklist/hosts.txt.gz"))
                } else {
                    context.assets.open("blocklist/hosts.txt")
                }
            }
            inputStream.use { BlocklistLoader.loadInto(it, newTrie) }
        }

        trie = newTrie
    }

    private fun getLegacyBlocklistFile(): File {
        val dir = File(context.filesDir, "blocklist")
        return File(dir, "hosts.txt")
    }

    fun getLastUpdatedTimestamp(): Long {
        return prefs.getLong("last_updated_timestamp", 0L)
    }

    fun setLastUpdatedTimestamp(ts: Long) {
        prefs.edit().putLong("last_updated_timestamp", ts).apply()
    }

    fun classify(domain: String): Verdict {
        val t = trie ?: return Verdict.Unknown
        return if (t.matches(domain)) Verdict.Tracker() else Verdict.Clean
    }

    fun domainCount(): Int = trie?.size ?: 0

    fun isReady(): Boolean = trie != null
}
