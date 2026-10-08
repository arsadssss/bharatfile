package com.bharatfile.app.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import com.bharatfile.app.model.ImageFormat
import com.bharatfile.app.model.ProcessedFileResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ImageResizer {

    suspend fun resizeImage(
        context: Context,
        inputUri: Uri,
        targetWidth: Int,
        targetHeight: Int,
        format: ImageFormat = ImageFormat.JPEG,
        quality: Int = 90
    ): ProcessedFileResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val originalFileName = FileHelper.getFileName(context, inputUri)
        val originalSize = FileHelper.getFileSize(context, inputUri)

        val inputStream = context.contentResolver.openInputStream(inputUri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close() ?: throw IllegalArgumentException("Cannot read image")

        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)
        if (scaledBitmap != originalBitmap) {
            originalBitmap.recycle()
        }

        val ext = format.extension
        val baseName = originalFileName.substringBeforeLast('.')
        val outputFile = File(context.cacheDir, "resized_${System.currentTimeMillis()}_$baseName.$ext")

        FileOutputStream(outputFile).use { out ->
            scaledBitmap.compress(format.compressFormat, quality, out)
        }
        scaledBitmap.recycle()

        val finalSize = outputFile.length()
        val duration = System.currentTimeMillis() - startTime

        ProcessedFileResult(
            originalUri = inputUri,
            fileName = "${baseName}_${targetWidth}x${targetHeight}.$ext",
            originalSizeBytes = originalSize,
            compressedSizeBytes = finalSize,
            outputFile = outputFile,
            mimeType = format.mimeType,
            processingTimeMs = duration
        )
    }

    suspend fun resizeToTargetKb(
        context: Context,
        inputUri: Uri,
        targetKb: Int,
        format: ImageFormat = ImageFormat.JPEG
    ): ProcessedFileResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val originalFileName = FileHelper.getFileName(context, inputUri)
        val originalSize = FileHelper.getFileSize(context, inputUri)
        val targetBytes = targetKb * 1024L

        val inputStream = context.contentResolver.openInputStream(inputUri)
        var currentBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close() ?: throw IllegalArgumentException("Cannot read image")

        var minQuality = 10
        var maxQuality = 95
        var bestQuality = 80
        var bestBytes = ByteArray(0)

        // Iterative binary search for optimal quality & dimension scale
        for (attempt in 0..4) {
            val baos = ByteArrayOutputStream()
            currentBitmap.compress(format.compressFormat, bestQuality, baos)
            val currentBytes = baos.toByteArray()

            if (currentBytes.size <= targetBytes) {
                bestBytes = currentBytes
                minQuality = bestQuality
                bestQuality = (minQuality + maxQuality) / 2
            } else {
                maxQuality = bestQuality
                bestQuality = (minQuality + maxQuality) / 2
                // If even at low quality it exceeds target, downsample dimensions by 20%
                if (bestQuality <= 25) {
                    val newWidth = (currentBitmap.width * 0.8).toInt().coerceAtLeast(100)
                    val newHeight = (currentBitmap.height * 0.8).toInt().coerceAtLeast(100)
                    val scaled = Bitmap.createScaledBitmap(currentBitmap, newWidth, newHeight, true)
                    if (scaled != currentBitmap) {
                        currentBitmap.recycle()
                        currentBitmap = scaled
                    }
                    minQuality = 20
                    maxQuality = 85
                    bestQuality = 60
                }
            }
        }

        if (bestBytes.isEmpty()) {
            val baos = ByteArrayOutputStream()
            currentBitmap.compress(format.compressFormat, 30, baos)
            bestBytes = baos.toByteArray()
        }
        currentBitmap.recycle()

        val ext = format.extension
        val baseName = originalFileName.substringBeforeLast('.')
        val outputFile = File(context.cacheDir, "target_${targetKb}kb_${System.currentTimeMillis()}_$baseName.$ext")
        FileOutputStream(outputFile).use { it.write(bestBytes) }

        val finalSize = outputFile.length()
        val duration = System.currentTimeMillis() - startTime

        ProcessedFileResult(
            originalUri = inputUri,
            fileName = "${baseName}_${targetKb}kb.$ext",
            originalSizeBytes = originalSize,
            compressedSizeBytes = finalSize,
            outputFile = outputFile,
            mimeType = format.mimeType,
            processingTimeMs = duration
        )
    }
}
