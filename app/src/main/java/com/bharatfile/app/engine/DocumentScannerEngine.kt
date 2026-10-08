package com.bharatfile.app.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import com.bharatfile.app.model.ProcessedFileResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class ScanFilter {
    ORIGINAL,
    MAGIC_COLOR,
    GRAYSCALE,
    BLACK_AND_WHITE
}

object DocumentScannerEngine {

    fun applyFilter(bitmap: Bitmap, filter: ScanFilter): Bitmap {
        return when (filter) {
            ScanFilter.ORIGINAL -> bitmap.copy(bitmap.config, true)
            ScanFilter.GRAYSCALE -> toGrayscale(bitmap)
            ScanFilter.MAGIC_COLOR -> toMagicColor(bitmap)
            ScanFilter.BLACK_AND_WHITE -> toBlackAndWhite(bitmap)
        }
    }

    private fun toGrayscale(bitmap: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        val colorMatrix = ColorMatrix().apply { setSaturation(0f) }
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    private fun toMagicColor(bitmap: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        // Boost contrast (1.3f) and brightness (15)
        val contrast = 1.35f
        val brightness = 15f
        val colorMatrix = ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, brightness,
                0f, contrast, 0f, 0f, brightness,
                0f, 0f, contrast, 0f, brightness,
                0f, 0f, 0f, 1f, 0f
            )
        )
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    private fun toBlackAndWhite(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val bwBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // Otsu / Adaptive threshold approximation for fast crisp document scanning
        var sumLuminance = 0L
        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            sumLuminance += (0.299 * r + 0.587 * g + 0.114 * b).toInt()
        }
        val threshold = (sumLuminance / pixels.size).toInt().coerceIn(110, 150)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            val luminance = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
            pixels[i] = if (luminance > threshold) Color.WHITE else Color.BLACK
        }

        bwBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bwBitmap
    }

    suspend fun saveScannedPagesToPdf(
        context: Context,
        pageBitmaps: List<Bitmap>,
        outputName: String = "BharatFile_Scan.pdf"
    ): ProcessedFileResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val tempImageUris = mutableListOf<Uri>()

        pageBitmaps.forEachIndexed { idx, bmp ->
            val tempFile = File(context.cacheDir, "scan_page_$idx.jpg")
            FileOutputStream(tempFile).use { out ->
                bmp.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            tempImageUris.add(Uri.fromFile(tempFile))
        }

        val result = ImageToPdfEngine.createPdfFromImages(context, tempImageUris, outputName, marginPt = 10f)

        // Clean up temp images
        tempImageUris.forEach { uri ->
            File(uri.path ?: "").delete()
        }

        result
    }
}
