package com.masum.lantern.data.blocklist

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BlocklistTrieTest {

    private lateinit var trie: BlocklistTrie

    @Before
    fun setup() {
        trie = BlocklistTrie()
        trie.insert("example.com")
        trie.insert("ads.tracker.net")
    }

    @Test
    fun `exact domain matches`() {
        assertTrue(trie.matches("example.com"))
    }

    @Test
    fun `subdomain matches via suffix`() {
        assertTrue(trie.matches("sub.example.com"))
        assertTrue(trie.matches("deep.sub.example.com"))
    }

    @Test
    fun `unrelated domain does not match`() {
        assertFalse(trie.matches("other.com"))
        assertFalse(trie.matches("notexample.com"))
    }

    @Test
    fun `partial suffix does not match parent`() {
        assertFalse(trie.matches("com"))
        assertFalse(trie.matches("tracker.net"))
    }

    @Test
    fun `exact multi-label entry matches`() {
        assertTrue(trie.matches("ads.tracker.net"))
    }

    @Test
    fun `subdomain of multi-label entry matches`() {
        assertTrue(trie.matches("sub.ads.tracker.net"))
    }

    @Test
    fun `empty domain does not match`() {
        assertFalse(trie.matches(""))
        assertFalse(trie.matches("."))
    }

    @Test
    fun `size tracks unique insertions`() {
        assertEquals(2, trie.size)
    }

    @Test
    fun `duplicate insertion does not increase size`() {
        trie.insert("example.com")
        assertEquals(2, trie.size)
    }

    @Test
    fun `case sensitivity - lowercase matches`() {
        trie.insert("UPPER.COM".lowercase())
        assertTrue(trie.matches("upper.com"))
    }
}
