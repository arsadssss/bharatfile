package com.bharatfile.app

import com.bharatfile.app.model.ProcessedFileResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ProcessedFileResultTest {

    @Test
    fun testFormatFileSize() {
        assertEquals("0 B", ProcessedFileResult.formatFileSize(0))
        assertEquals("500 B", ProcessedFileResult.formatFileSize(500))
        assertEquals("890 KB", ProcessedFileResult.formatFileSize(890 * 1024))
        assertEquals("2.4 MB", ProcessedFileResult.formatFileSize((2.4 * 1024 * 1024).toLong()))
    }

    @Test
    fun testSavingsCalculation() {
        val original = (2.4 * 1024 * 1024).toLong()
        val compressed = (890 * 1024).toLong()

        val result = ProcessedFileResult(
            originalUri = null,
            fileName = "document.pdf",
            originalSizeBytes = original,
            compressedSizeBytes = compressed,
            outputFile = File("dummy.pdf"),
            mimeType = "application/pdf"
        )

        // 2.4 MB = 2,516,582 bytes, 890 KB = 911,360 bytes
        // (2516582 - 911360) / 2516582 = ~63%
        assertEquals(63, result.savingsPercentage)
        assertEquals("63% smaller", result.savingsPercentageString)
    }

    @Test
    fun testTargetSizeComparison() {
        val original = 1024L * 1024L // 1 MB
        val target = 200L * 1024L // 200 KB
        val compressed = 180L * 1024L // 180 KB (under target)

        val result = ProcessedFileResult(
            originalUri = null,
            fileName = "marksheet.pdf",
            originalSizeBytes = original,
            compressedSizeBytes = compressed,
            outputFile = File("dummy.pdf"),
            mimeType = "application/pdf",
            targetSizeBytes = target,
            isTargetMode = true
        )

        assertEquals("200 KB", result.formattedTargetSize)
        val comparison = result.targetComparisonString
        assertNotNull(comparison)
        assertTrue(comparison!!.contains("under target"))
        assertTrue(comparison.contains("Target: 200 KB"))
        assertTrue(comparison.contains("Actual: 180 KB"))
    }
}
