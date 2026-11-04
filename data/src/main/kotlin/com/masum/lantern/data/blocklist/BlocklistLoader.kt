package com.masum.lantern.data.blocklist

import java.io.InputStream

internal object BlocklistLoader {

    private val SKIP = setOf(
        "localhost", "0.0.0.0", "broadcasthost", "local", "ip6-localhost",
        "ip6-loopback", "ip6-localnet", "ip6-mcastprefix", "ip6-allnodes",
        "ip6-allrouters", "ip6-allhosts"
    )

    fun load(stream: InputStream): BlocklistTrie {
        val trie = BlocklistTrie()
        loadInto(stream, trie)
        return trie
    }

    fun loadInto(stream: InputStream, trie: BlocklistTrie) {
        stream.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                parseDomain(line)?.let { trie.insert(it) }
            }
        }
    }

    internal fun parseDomain(line: String): String? {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.startsWith('#') || trimmed.startsWith('!')) return null

        val cleanLine = if (trimmed.startsWith("||") && trimmed.endsWith("^")) {
            trimmed.substring(2, trimmed.length - 1)
        } else {
            trimmed
        }

        val spaceIdx = cleanLine.indexOfFirst { it == ' ' || it == '\t' }
        val candidate = if (spaceIdx != -1) {
            cleanLine.substring(spaceIdx + 1).trim()
        } else {
            cleanLine
        }

        val domain = candidate.takeWhile { it != '#' && it != ' ' && it != '\t' && it != '^' }.lowercase()
        if (domain.isEmpty() || domain in SKIP) return null
        if (!domain.contains('.') || domain.startsWith('.') || domain.endsWith('.')) return null
        if (domain.contains('/') || domain.contains(':') || domain.contains('*')) return null

        return domain
    }
}
