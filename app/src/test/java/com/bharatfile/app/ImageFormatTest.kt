package com.bharatfile.app

import com.bharatfile.app.model.ImageFormat
import org.junit.Assert.assertEquals
import org.junit.Test

class ImageFormatTest {

    @Test
    fun testFromExtension() {
        assertEquals(ImageFormat.JPEG, ImageFormat.fromExtension("jpg"))
        assertEquals(ImageFormat.JPEG, ImageFormat.fromExtension("jpeg"))
        assertEquals(ImageFormat.PNG, ImageFormat.fromExtension("png"))
        assertEquals(ImageFormat.WEBP, ImageFormat.fromExtension("webp"))
        assertEquals(ImageFormat.JPEG, ImageFormat.fromExtension("unknown"))
    }
}
