package com.masum.vigil.data.blocklist

internal class BlocklistTrie {

    private val root = TrieNode()
    private var _size = 0

    val size: Int get() = _size

    fun insert(domain: String) {
        val labels = domain.split('.').filter { it.isNotEmpty() }
        if (labels.isEmpty()) return
        var node = root
        for (label in labels.reversed()) {
            node = node.children.getOrPut(label) { TrieNode() }
        }
        if (!node.isEnd) {
            node.isEnd = true
            _size++
        }
    }

    fun matches(domain: String): Boolean {
        val labels = domain.split('.').filter { it.isNotEmpty() }
        if (labels.isEmpty()) return false
        var node = root
        for (label in labels.reversed()) {
            if (node.isEnd) return true
            node = node.children[label] ?: return false
        }
        return node.isEnd
    }

    private class TrieNode {
        val children = HashMap<String, TrieNode>()
        var isEnd = false
    }
}
