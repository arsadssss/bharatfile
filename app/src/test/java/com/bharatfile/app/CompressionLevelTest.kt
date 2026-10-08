package com.bharatfile.app

import com.bharatfile.app.model.CompressionLevel
import org.junit.Assert.assertEquals
import org.junit.Test

class CompressionLevelTest {

    @Test
    fun testFromProgress() {
        assertEquals(CompressionLevel.LOW, CompressionLevel.fromProgress(0.1f))
        assertEquals(CompressionLevel.MEDIUM, CompressionLevel.fromProgress(0.5f))
        assertEquals(CompressionLevel.HIGH, CompressionLevel.fromProgress(0.9f))
    }

    @Test
    fun testFactors() {
        // Low should have higher quality factor than Medium and High
        assert(CompressionLevel.LOW.qualityFactor > CompressionLevel.MEDIUM.qualityFactor)
        assert(CompressionLevel.MEDIUM.qualityFactor > CompressionLevel.HIGH.qualityFactor)
    }
}
