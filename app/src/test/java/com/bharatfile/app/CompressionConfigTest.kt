package com.bharatfile.app

import com.bharatfile.app.model.CompressionConfig
import com.bharatfile.app.model.CompressionMode
import com.bharatfile.app.model.SizeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CompressionConfigTest {

    @Test
    fun testPercentageValidation() {
        val validConfig = CompressionConfig(
            mode = CompressionMode.PERCENTAGE,
            percentage = 67
        )
        assertNull(validConfig.validate())

        val boundaryMin = CompressionConfig(
            mode = CompressionMode.PERCENTAGE,
            percentage = 1
        )
        assertNull(boundaryMin.validate())

        val boundaryMax = CompressionConfig(
            mode = CompressionMode.PERCENTAGE,
            percentage = 100
        )
        assertNull(boundaryMax.validate())

        val invalidZero = CompressionConfig(
            mode = CompressionMode.PERCENTAGE,
            percentage = 0
        )
        assertNotNull(invalidZero.validate())

        val invalidOver = CompressionConfig(
            mode = CompressionMode.PERCENTAGE,
            percentage = 101
        )
        assertNotNull(invalidOver.validate())
    }

    @Test
    fun testTargetSizeBytesCalculation() {
        val config50Kb = CompressionConfig(
            mode = CompressionMode.TARGET_SIZE,
            targetValue = "50",
            targetUnit = SizeUnit.KB
        )
        assertEquals(50L * 1024L, config50Kb.targetBytes)

        val config2Mb = CompressionConfig(
            mode = CompressionMode.TARGET_SIZE,
            targetValue = "2.5",
            targetUnit = SizeUnit.MB
        )
        assertEquals((2.5 * 1024 * 1024).toLong(), config2Mb.targetBytes)

        val invalidTarget = CompressionConfig(
            mode = CompressionMode.TARGET_SIZE,
            targetValue = "-5",
            targetUnit = SizeUnit.KB
        )
        assertNull(invalidTarget.targetBytes)
        assertNotNull(invalidTarget.validate())
    }

    @Test
    fun testTargetSizeValidationAgainstOriginal() {
        val originalSize = 1024L * 1024L // 1 MB

        // Target smaller than original -> valid
        val validSmaller = CompressionConfig(
            mode = CompressionMode.TARGET_SIZE,
            targetValue = "500",
            targetUnit = SizeUnit.KB
        )
        assertNull(validSmaller.validateAgainstOriginal(originalSize))

        // Target larger than original -> error message
        val invalidLarger = CompressionConfig(
            mode = CompressionMode.TARGET_SIZE,
            targetValue = "2",
            targetUnit = SizeUnit.MB
        )
        val error = invalidLarger.validateAgainstOriginal(originalSize)
        assertNotNull(error)
        assertTrue(error!!.contains("larger than or equal to original"))

        // Target equal to original -> error message
        val invalidEqual = CompressionConfig(
            mode = CompressionMode.TARGET_SIZE,
            targetValue = "1",
            targetUnit = SizeUnit.MB
        )
        assertNotNull(invalidEqual.validateAgainstOriginal(originalSize))
    }

    @Test
    fun testQuickPresets() {
        assertEquals(8, CompressionConfig.QUICK_PRESETS.size)

        val first = CompressionConfig.QUICK_PRESETS[0]
        assertEquals("50", first.first)
        assertEquals(SizeUnit.KB, first.second)

        val last = CompressionConfig.QUICK_PRESETS[7]
        assertEquals("10", last.first)
        assertEquals(SizeUnit.MB, last.second)
    }

    @Test
    fun testSizeUnitFromString() {
        assertEquals(SizeUnit.MB, SizeUnit.fromString("mb"))
        assertEquals(SizeUnit.MB, SizeUnit.fromString("MB"))
        assertEquals(SizeUnit.KB, SizeUnit.fromString("kb"))
        assertEquals(SizeUnit.KB, SizeUnit.fromString("KB"))
        assertEquals(SizeUnit.KB, SizeUnit.fromString("anything_else"))
    }
}
