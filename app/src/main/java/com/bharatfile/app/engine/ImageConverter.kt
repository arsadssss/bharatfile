package com.bharatfile.app.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import com.bharatfile.app.model.ImageFormat
import com.bharatfile.app.model.ProcessedFileResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ImageConverter {

    suspend fun convertImage(
        context: Context,
        inputUri: Uri,
        targetFormat: ImageFormat,
        quality: Int = 90
    ): ProcessedFileResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val originalFileName = FileHelper.getFileName(context, inputUri)
        val originalSize = FileHelper.getFileSize(context, inputUri)

        val inputStream = context.contentResolver.openInputStream(inputUri)
        val sourceBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close() ?: throw IllegalArgumentException("Cannot decode source image")

        // If converting transparent image to JPEG, composite over white background
        val processedBitmap = if (targetFormat == ImageFormat.JPEG && sourceBitmap.hasAlpha()) {
            val nonAlphaBitmap = Bitmap.createBitmap(sourceBitmap.width, sourceBitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(nonAlphaBitmap)
            canvas.drawColor(Color.WHITE)
            canvas.drawBitmap(sourceBitmap, 0f, 0f, null)
            sourceBitmap.recycle()
            nonAlphaBitmap
        } else {
            sourceBitmap
        }

        val baseName = originalFileName.substringBeforeLast('.')
        val ext = targetFormat.extension
        val outputFile = File(context.cacheDir, "converted_${System.currentTimeMillis()}_$baseName.$ext")

        FileOutputStream(outputFile).use { outStream ->
            processedBitmap.compress(targetFormat.compressFormat, quality, outStream)
        }
        processedBitmap.recycle()

        val finalSize = outputFile.length()
        val duration = System.currentTimeMillis() - startTime

        ProcessedFileResult(
            originalUri = inputUri,
            fileName = "$baseName.$ext",
            originalSizeBytes = originalSize,
            compressedSizeBytes = finalSize,
            outputFile = outputFile,
            mimeType = targetFormat.mimeType,
            processingTimeMs = duration
        )
    }
}
