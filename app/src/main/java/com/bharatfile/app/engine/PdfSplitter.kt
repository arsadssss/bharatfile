package com.bharatfile.app.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.bharatfile.app.model.ProcessedFileResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object PdfSplitter {

    fun parsePageRanges(rangeString: String, maxPages: Int): List<Int> {
        val pages = mutableSetOf<Int>()
        val parts = rangeString.split(",")
        for (part in parts) {
            val trimmed = part.trim()
            if (trimmed.contains("-")) {
                val bounds = trimmed.split("-")
                val start = bounds.getOrNull(0)?.trim()?.toIntOrNull() ?: 1
                val end = bounds.getOrNull(1)?.trim()?.toIntOrNull() ?: maxPages
                for (p in start..end) {
                    if (p in 1..maxPages) pages.add(p)
                }
            } else {
                val single = trimmed.toIntOrNull()
                if (single != null && single in 1..maxPages) {
                    pages.add(single)
                }
            }
        }
        return pages.sorted()
    }

    suspend fun splitPdf(
        context: Context,
        inputUri: Uri,
        pageNumbers: List<Int>, // 1-indexed
        outputName: String = "BharatFile_Split.pdf"
    ): ProcessedFileResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val originalFileName = FileHelper.getFileName(context, inputUri)
        val originalSize = FileHelper.getFileSize(context, inputUri)

        val tempFile = FileHelper.copyUriToCacheFile(context, inputUri, "split_in_")
        val pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)

        val splitDoc = PdfDocument()
        val outputFile = File(context.cacheDir, "split_${System.currentTimeMillis()}_$outputName")

        try {
            pageNumbers.forEachIndexed { newIndex, pageNum ->
                val pZeroIndex = pageNum - 1
                if (pZeroIndex in 0 until renderer.pageCount) {
                    val page = renderer.openPage(pZeroIndex)
                    val width = page.width
                    val height = page.height

                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    canvas.drawColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                    page.close()

                    val pageInfo = PdfDocument.PageInfo.Builder(width, height, newIndex + 1).create()
                    val newPage = splitDoc.startPage(pageInfo)
                    newPage.canvas.drawBitmap(bitmap, null, Rect(0, 0, width, height), null)
                    splitDoc.finishPage(newPage)
                    bitmap.recycle()
                }
            }

            FileOutputStream(outputFile).use { out ->
                splitDoc.writeTo(out)
            }
        } finally {
            splitDoc.close()
            renderer.close()
            pfd.close()
            tempFile.delete()
        }

        val finalSize = outputFile.length()
        val duration = System.currentTimeMillis() - startTime

        ProcessedFileResult(
            originalUri = inputUri,
            fileName = outputName,
            originalSizeBytes = originalSize,
            compressedSizeBytes = finalSize,
            outputFile = outputFile,
            mimeType = "application/pdf",
            processingTimeMs = duration
        )
    }
}
