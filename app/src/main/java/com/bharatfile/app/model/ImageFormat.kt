package com.bharatfile.app.model

import android.graphics.Bitmap

enum class ImageFormat(
    val extension: String,
    val mimeType: String,
    val compressFormat: Bitmap.CompressFormat
) {
    JPEG("jpg", "image/jpeg", Bitmap.CompressFormat.JPEG),
    PNG("png", "image/png", Bitmap.CompressFormat.PNG),
    WEBP("webp", "image/webp", Bitmap.CompressFormat.WEBP);

    companion object {
        fun fromExtension(ext: String?): ImageFormat {
            return when (ext?.lowercase()) {
                "png" -> PNG
                "webp" -> WEBP
                else -> JPEG
            }
        }
    }
}
