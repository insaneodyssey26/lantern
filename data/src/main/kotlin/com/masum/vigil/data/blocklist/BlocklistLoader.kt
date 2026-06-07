package com.masum.vigil.data.blocklist

import java.io.InputStream

internal object BlocklistLoader {

    private val SKIP = setOf("localhost", "0.0.0.0", "broadcasthost", "local", "ip6-localhost",
        "ip6-loopback", "ip6-localnet", "ip6-mcastprefix", "ip6-allnodes",
        "ip6-allrouters", "ip6-allhosts")

    fun load(stream: InputStream): BlocklistTrie {
        val trie = BlocklistTrie()
        stream.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                parseDomain(line)?.let { trie.insert(it) }
            }
        }
        return trie
    }

    internal fun parseDomain(line: String): String? {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.startsWith('#')) return null
        val spaceIdx = trimmed.indexOfFirst { it == ' ' || it == '\t' }
        if (spaceIdx == -1) return null
        val domain = trimmed.substring(spaceIdx + 1).trim().lowercase()
        if (domain.isEmpty() || domain.startsWith('#')) return null
        if (domain in SKIP) return null
        if (!domain.contains('.')) return null
        return domain
    }
}
