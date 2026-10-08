package com.bharatfile.app.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.bharatfile.app.model.CompressionConfig
import com.bharatfile.app.model.CompressionLevel
import com.bharatfile.app.model.CompressionMode
import com.bharatfile.app.model.ProcessedFileResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

object PdfCompressor {

    suspend fun compressWithConfig(
        context: Context,
        inputUri: Uri,
        config: CompressionConfig,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): ProcessedFileResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val originalFileName = FileHelper.getFileName(context, inputUri)
        val originalSize = FileHelper.getFileSize(context, inputUri)

        currentCoroutineContext().ensureActive()
        onProgress(0.05f, "Analyzing PDF structure…")

        val tempInputFile = FileHelper.copyUriToCacheFile(context, inputUri, "pdf_in_")
        val pfd = ParcelFileDescriptor.open(tempInputFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        val pageCount = renderer.pageCount

        val outputFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}_$originalFileName")
        val newPdfDocument = PdfDocument()

        // Determine scale and quality based on config
        val (scaleFactor, jpegQuality) = when (config.mode) {
            CompressionMode.PERCENTAGE -> {
                val p = config.percentage.coerceIn(1, 100)
                val scale = (1.0f - (p / 100f * 0.55f)).coerceIn(0.40f, 1.0f)
                val quality = (95 - (p * 0.75f)).roundToInt().coerceIn(20, 92)
                Pair(scale, quality)
            }
            CompressionMode.TARGET_SIZE -> {
                val target = config.targetBytes ?: (originalSize * 0.5).toLong()
                val ratio = (target.toDouble() / originalSize.toDouble()).coerceIn(0.10, 1.0)
                when {
                    ratio < 0.25 -> Pair(0.45f, 28)
                    ratio < 0.50 -> Pair(0.62f, 48)
                    ratio < 0.75 -> Pair(0.80f, 68)
                    else -> Pair(0.95f, 85)
                }
            }
        }

        try {
            for (i in 0 until pageCount) {
                currentCoroutineContext().ensureActive()
                val pageProgressStart = 0.1f + (i.toFloat() / pageCount.toFloat()) * 0.8f
                onProgress(pageProgressStart, "Processing page ${i + 1} of $pageCount…")

                val page = renderer.openPage(i)
                val targetWidth = (page.width * scaleFactor).roundToInt().coerceAtLeast(100)
                val targetHeight = (page.height * scaleFactor).roundToInt().coerceAtLeast(100)

                val pageBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(pageBitmap)
                canvas.drawColor(Color.WHITE)
                page.render(pageBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                page.close()

                // Re-encode via JPEG compression
                val stream = ByteArrayOutputStream()
                pageBitmap.compress(Bitmap.CompressFormat.JPEG, jpegQuality, stream)
                pageBitmap.recycle()

                currentCoroutineContext().ensureActive()
                val compressedBytes = stream.toByteArray()
                val compressedBitmap = BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size)

                // Write into new PdfDocument page matching original dimensions
                val pageInfo = PdfDocument.PageInfo.Builder(page.width, page.height, i + 1).create()
                val newPage = newPdfDocument.startPage(pageInfo)
                val destRect = Rect(0, 0, page.width, page.height)
                newPage.canvas.drawBitmap(compressedBitmap, null, destRect, Paint(Paint.FILTER_BITMAP_FLAG))
                newPdfDocument.finishPage(newPage)
                compressedBitmap.recycle()

                val pageProgressEnd = 0.1f + ((i + 1).toFloat() / pageCount.toFloat()) * 0.8f
                onProgress(pageProgressEnd, "Page ${i + 1} compressed")
            }

            currentCoroutineContext().ensureActive()
            onProgress(0.92f, "Finalizing compressed PDF…")
            FileOutputStream(outputFile).use { out ->
                newPdfDocument.writeTo(out)
            }
        } catch (e: Exception) {
            outputFile.delete()
            throw e
        } finally {
            newPdfDocument.close()
            renderer.close()
            pfd.close()
            tempInputFile.delete()
        }

        currentCoroutineContext().ensureActive()
        onProgress(1.0f, "Compression Complete")
        val finalSize = outputFile.length()
        val duration = System.currentTimeMillis() - startTime

        ProcessedFileResult(
            originalUri = inputUri,
            fileName = originalFileName,
            originalSizeBytes = originalSize,
            compressedSizeBytes = finalSize,
            outputFile = outputFile,
            mimeType = "application/pdf",
            processingTimeMs = duration,
            targetSizeBytes = if (config.mode == CompressionMode.TARGET_SIZE) config.targetBytes else null,
            isTargetMode = (config.mode == CompressionMode.TARGET_SIZE)
        )
    }

    suspend fun compressPdf(
        context: Context,
        inputUri: Uri,
        level: CompressionLevel,
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
        return compressWithConfig(context, inputUri, config, onProgress)
    }

    fun estimateCompressedSize(originalSize: Long, config: CompressionConfig): Long {
        if (originalSize <= 0) return 0
        return when (config.mode) {
            CompressionMode.PERCENTAGE -> {
                val p = config.percentage.coerceIn(1, 100)
                val ratio = (1.0f - (p / 100f * 0.75f)).coerceIn(0.18f, 0.95f)
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
