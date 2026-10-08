package com.bharatfile.app

import com.bharatfile.app.engine.PassportPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PassportPresetTest {

    @Test
    fun testIndianPassportAspectRatio() {
        val preset = PassportPreset.INDIAN_PASSPORT
        assertEquals(7, preset.aspectWidth)
        assertEquals(9, preset.aspectHeight)
        // 3.5cm / 4.5cm is 7/9
        assertTrue((preset.aspectWidth.toFloat() / preset.aspectHeight.toFloat()) < 1.0f)
    }

    @Test
    fun testStampSizeRatio() {
        val preset = PassportPreset.STAMP_SIZE
        assertEquals(5, preset.aspectWidth)
        assertEquals(6, preset.aspectHeight)
    }

    @Test
    fun testSquareRatio() {
        val preset = PassportPreset.SQUARE_US_VISA
        assertEquals(1, preset.aspectWidth)
        assertEquals(1, preset.aspectHeight)
    }
}
