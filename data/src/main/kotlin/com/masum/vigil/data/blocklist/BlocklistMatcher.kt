package com.masum.vigil.data.blocklist

import android.content.Context
import com.masum.vigil.core.common.dispatcher.DispatcherProvider
import com.masum.vigil.domain.model.Verdict
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlocklistMatcher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: DispatcherProvider,
) {
    @Volatile private var trie: BlocklistTrie? = null

    suspend fun load() = withContext(dispatchers.io) {
        val loaded = context.assets.open("blocklist/hosts.txt").use { BlocklistLoader.load(it) }
        trie = loaded
    }

    fun classify(domain: String): Verdict {
        val t = trie ?: return Verdict.Unknown
        return if (t.matches(domain)) Verdict.Tracker() else Verdict.Clean
    }

    fun domainCount(): Int = trie?.size ?: 0

    fun isReady(): Boolean = trie != null
}
