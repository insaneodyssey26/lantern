package com.masum.lantern.data.blocklist

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BlocklistLoaderTest {

    @Test
    fun `parses standard hosts line`() {
        assertEquals("ads.example.com", BlocklistLoader.parseDomain("0.0.0.0 ads.example.com"))
    }

    @Test
    fun `parses tab-separated line`() {
        assertEquals("tracker.net", BlocklistLoader.parseDomain("0.0.0.0\ttracker.net"))
    }

    @Test
    fun `ignores comment lines`() {
        assertNull(BlocklistLoader.parseDomain("# this is a comment"))
    }

    @Test
    fun `ignores empty lines`() {
        assertNull(BlocklistLoader.parseDomain(""))
        assertNull(BlocklistLoader.parseDomain("   "))
    }

    @Test
    fun `ignores localhost`() {
        assertNull(BlocklistLoader.parseDomain("127.0.0.1 localhost"))
    }

    @Test
    fun `ignores reserved entries`() {
        assertNull(BlocklistLoader.parseDomain("0.0.0.0 broadcasthost"))
        assertNull(BlocklistLoader.parseDomain("0.0.0.0 local"))
    }

    @Test
    fun `ignores single-label domains`() {
        assertNull(BlocklistLoader.parseDomain("0.0.0.0 noperiod"))
    }

    @Test
    fun `normalises to lowercase`() {
        assertEquals("ads.example.com", BlocklistLoader.parseDomain("0.0.0.0 ADS.EXAMPLE.COM"))
    }

    @Test
    fun `ignores line with no space`() {
        assertNull(BlocklistLoader.parseDomain("0.0.0.0"))
    }

    @Test
    fun `loads stream and returns populated trie`() {
        val input = """
            # comment
            0.0.0.0 ads.example.com
            0.0.0.0 tracker.net
            127.0.0.1 localhost
        """.trimIndent().byteInputStream()
        val trie = BlocklistLoader.load(input)
        assertEquals(2, trie.size)
        assertTrue(trie.matches("ads.example.com"))
        assertTrue(trie.matches("sub.tracker.net"))
    }
}
