package com.bharatfile.app

import com.bharatfile.app.engine.PdfSplitter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PdfSplitterTest {

    @Test
    fun testParseSimpleRanges() {
        val result = PdfSplitter.parsePageRanges("1-3", maxPages = 10)
        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun testParseMixedPagesAndRanges() {
        val result = PdfSplitter.parsePageRanges("1-2, 5, 7-8", maxPages = 10)
        assertEquals(listOf(1, 2, 5, 7, 8), result)
    }

    @Test
    fun testParseOutOfBounds() {
        val result = PdfSplitter.parsePageRanges("8-15", maxPages = 10)
        assertEquals(listOf(8, 9, 10), result)
    }

    @Test
    fun testParseInvalidInput() {
        val result = PdfSplitter.parsePageRanges("abc, xyz", maxPages = 10)
        assertTrue(result.isEmpty())
    }
}
