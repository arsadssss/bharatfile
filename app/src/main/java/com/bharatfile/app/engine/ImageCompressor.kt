package com.bharatfile.app.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import com.bharatfile.app.model.CompressionConfig
import com.bharatfile.app.model.CompressionLevel
import com.bharatfile.app.model.CompressionMode
import com.bharatfile.app.model.ImageFormat
import com.bharatfile.app.model.ProcessedFileResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt
import kotlin.math.sqrt

object ImageCompressor {

    suspend fun compressWithConfig(
        context: Context,
        inputUri: Uri,
        config: CompressionConfig,
        targetFormat: ImageFormat = ImageFormat.JPEG,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): ProcessedFileResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val originalFileName = FileHelper.getFileName(context, inputUri)
        val originalSize = FileHelper.getFileSize(context, inputUri)

        currentCoroutineContext().ensureActive()
        onProgress(0.12f, "Reading image data…")

        val orientation = getExifOrientation(context, inputUri)
        val inputStream = context.contentResolver.openInputStream(inputUri)
        val rawBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        if (rawBitmap == null) {
            throw IllegalArgumentException("Unable to decode image from selected file.")
        }

        currentCoroutineContext().ensureActive()
        onProgress(0.30f, "Aligning orientation & pixels…")

        val baseBitmap = if (orientation != 0) {
            val matrix = Matrix().apply { postRotate(orientation.toFloat()) }
            Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true).also {
                if (it != rawBitmap) rawBitmap.recycle()
            }
        } else {
            rawBitmap
        }

        val ext = targetFormat.extension
        val baseName = originalFileName.substringBeforeLast('.')
        val outputFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}_$baseName.$ext")

        try {
            when (config.mode) {
                CompressionMode.PERCENTAGE -> {
                    val p = config.percentage.coerceIn(1, 100)
                    val quality = (98 - (p * 0.82f)).roundToInt().coerceIn(15, 96)
                    val scaleFactor = (1.0f - (p / 100f * 0.55f)).coerceIn(0.40f, 1.0f)

                    onProgress(0.55f, "Scaling resolution (Scale: ${(scaleFactor * 100).toInt()}%)…")
                    currentCoroutineContext().ensureActive()

                    val scaledBitmap = if (scaleFactor < 0.98f) {
                        val targetW = (baseBitmap.width * scaleFactor).roundToInt().coerceAtLeast(50)
                        val targetH = (baseBitmap.height * scaleFactor).roundToInt().coerceAtLeast(50)
                        Bitmap.createScaledBitmap(baseBitmap, targetW, targetH, true).also {
                            if (it != baseBitmap) baseBitmap.recycle()
                        }
                    } else {
                        baseBitmap
                    }

                    onProgress(0.80f, "Compressing JPEG at $quality% quality…")
                    currentCoroutineContext().ensureActive()

                    FileOutputStream(outputFile).use { outStream ->
                        scaledBitmap.compress(targetFormat.compressFormat, quality, outStream)
                    }
                    scaledBitmap.recycle()
                }

                CompressionMode.TARGET_SIZE -> {
                    val targetBytes = config.targetBytes ?: (originalSize * 0.5).toLong()
                    onProgress(0.45f, "Calculating target compression budget…")

                    val bestBytes = compressToTargetIterative(
                        bitmap = baseBitmap,
                        targetFormat = targetFormat,
                        targetBytes = targetBytes,
                        onProgress = onProgress
                    )
                    baseBitmap.recycle()

                    FileOutputStream(outputFile).use { out ->
                        out.write(bestBytes)
                    }
                }
            }

            currentCoroutineContext().ensureActive()
            onProgress(1.0f, "Compression Complete")

            val finalSize = outputFile.length()
            val duration = System.currentTimeMillis() - startTime

            ProcessedFileResult(
                originalUri = inputUri,
                fileName = "$baseName.$ext",
                originalSizeBytes = originalSize,
                compressedSizeBytes = finalSize,
                outputFile = outputFile,
                mimeType = targetFormat.mimeType,
                processingTimeMs = duration,
                targetSizeBytes = if (config.mode == CompressionMode.TARGET_SIZE) config.targetBytes else null,
                isTargetMode = (config.mode == CompressionMode.TARGET_SIZE)
            )
        } catch (e: Exception) {
            outputFile.delete()
            throw e
        }
    }

    private suspend fun compressToTargetIterative(
        bitmap: Bitmap,
        targetFormat: ImageFormat,
        targetBytes: Long,
        onProgress: (Float, String) -> Unit
    ): ByteArray {
        var currentBitmap = bitmap
        val stream = ByteArrayOutputStream()

        currentCoroutineContext().ensureActive()
        onProgress(0.55f, "Target pass 1: Initial encoding…")
        bitmap.compress(targetFormat.compressFormat, 80, stream)
        var currentBytes = stream.toByteArray()

        if (currentBytes.size <= targetBytes) {
            var lowQ = 80
            var highQ = 96
            var bestUnder = currentBytes

            for (attempt in 1..3) {
                currentCoroutineContext().ensureActive()
                val midQ = (lowQ + highQ) / 2
                stream.reset()
                bitmap.compress(targetFormat.compressFormat, midQ, stream)
                val trial = stream.toByteArray()
                if (trial.size <= targetBytes) {
                    bestUnder = trial
                    lowQ = midQ + 1
                } else {
                    highQ = midQ - 1
                }
                if (lowQ > highQ) break
            }
            return bestUnder
        }

        val sizeRatio = targetBytes.toDouble() / currentBytes.size.toDouble()
        if (sizeRatio < 0.65) {
            val scale = sqrt(sizeRatio * 1.15).coerceIn(0.30, 0.90).toFloat()
            val newW = (bitmap.width * scale).roundToInt().coerceAtLeast(80)
            val newH = (bitmap.height * scale).roundToInt().coerceAtLeast(80)
            onProgress(0.68f, "Target pass 2: Scaling resolution…")
            currentBitmap = Bitmap.createScaledBitmap(bitmap, newW, newH, true)
        }

        var lowQ = 15
        var highQ = 85
        var bestBytes: ByteArray = currentBytes
        var closestDiff = Long.MAX_VALUE

        for (attempt in 1..4) {
            currentCoroutineContext().ensureActive()
            val midQ = (lowQ + highQ) / 2
            onProgress(0.70f + (attempt * 0.06f), "Target pass ${attempt + 2}: Optimizing ($midQ%)…")
            stream.reset()
            currentBitmap.compress(targetFormat.compressFormat, midQ, stream)
            val trial = stream.toByteArray()
            val diff = trial.size - targetBytes

            if (trial.size <= targetBytes) {
                bestBytes = trial
                lowQ = midQ + 1
            } else {
                if (diff < closestDiff) {
                    closestDiff = diff
                    bestBytes = trial
                }
                highQ = midQ - 1
            }
            if (lowQ > highQ) break
        }

        if (currentBitmap != bitmap) {
            currentBitmap.recycle()
        }

        return bestBytes
    }

    suspend fun compressImage(
        context: Context,
        inputUri: Uri,
        level: CompressionLevel,
        targetFormat: ImageFormat = ImageFormat.JPEG,
        customQuality: Int? = null,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): ProcessedFileResult {
        val percentage = when (level) {
            CompressionLevel.LOW -> 30
            CompressionLevel.MEDIUM -> 65
            CompressionLevel.HIGH -> 88
        }
        val config = CompressionConfig(
            mode = CompressionMode.PERCENTAGE,
            percentage = percentage
        )
        return compressWithConfig(context, inputUri, config, targetFormat, onProgress)
    }

    private fun getExifOrientation(context: Context, uri: Uri): Int {
        var orientation = 0
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                orientation = when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            }
        } catch (_: Exception) {}
        return orientation
    }

    fun estimateCompressedSize(originalSize: Long, config: CompressionConfig): Long {
        if (originalSize <= 0) return 0
        return when (config.mode) {
            CompressionMode.PERCENTAGE -> {
                val p = config.percentage.coerceIn(1, 100)
                val ratio = (1.0f - (p / 100f * 0.78f)).coerceIn(0.15f, 0.95f)
                (originalSize * ratio).toLong().coerceAtLeast(1024L)
            }
            CompressionMode.TARGET_SIZE -> {
                val target = config.targetBytes ?: (originalSize / 2)
                target.coerceAtMost(originalSize).coerceAtLeast(1024L)
            }
        }
    }

    fun estimateCompressedSize(originalSize: Long, level: CompressionLevel): Long {
        val percentage = when (level) {
            CompressionLevel.LOW -> 30
            CompressionLevel.MEDIUM -> 65
            CompressionLevel.HIGH -> 88
        }
        return estimateCompressedSize(originalSize, CompressionConfig(percentage = percentage))
    }
}
