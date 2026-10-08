package com.bharatfile.app.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
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

data class PdfPageItem(
    val originalPageIndex: Int, // 0-indexed
    val thumbnail: Bitmap,
    val width: Int,
    val height: Int,
    val rotationDegrees: Int = 0
)

object PdfPageManager {

    suspend fun loadPages(context: Context, pdfUri: Uri): List<PdfPageItem> = withContext(Dispatchers.IO) {
        val tempFile = FileHelper.copyUriToCacheFile(context, pdfUri, "org_in_")
        val pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        val items = mutableListOf<PdfPageItem>()

        try {
            for (i in 0 until renderer.pageCount) {
                val page = renderer.openPage(i)
                val thumbWidth = 200
                val thumbHeight = (200f * (page.height.toFloat() / page.width.toFloat())).toInt()

                val bitmap = Bitmap.createBitmap(thumbWidth, thumbHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                items.add(
                    PdfPageItem(
                        originalPageIndex = i,
                        thumbnail = bitmap,
                        width = page.width,
                        height = page.height,
                        rotationDegrees = 0
                    )
                )
                page.close()
            }
        } finally {
            renderer.close()
            pfd.close()
            tempFile.delete()
        }
        items
    }

    suspend fun saveOrganizedPdf(
        context: Context,
        originalUri: Uri,
        orderedPages: List<PdfPageItem>,
        outputName: String = "BharatFile_Organized.pdf"
    ): ProcessedFileResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val originalSize = FileHelper.getFileSize(context, originalUri)

        val tempFile = FileHelper.copyUriToCacheFile(context, originalUri, "org_save_")
        val pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)

        val doc = PdfDocument()
        val outputFile = File(context.cacheDir, "organized_${System.currentTimeMillis()}_$outputName")

        try {
            orderedPages.forEachIndexed { newIndex, pageItem ->
                val page = renderer.openPage(pageItem.originalPageIndex)
                var width = page.width
                var height = page.height

                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                page.close()

                val finalBitmap: Bitmap
                if (pageItem.rotationDegrees % 360 != 0) {
                    val matrix = Matrix().apply { postRotate(pageItem.rotationDegrees.toFloat()) }
                    finalBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
                    bitmap.recycle()
                    if (pageItem.rotationDegrees % 180 != 0) {
                        val tmp = width
                        width = height
                        height = tmp
                    }
                } else {
                    finalBitmap = bitmap
                }

                val pageInfo = PdfDocument.PageInfo.Builder(width, height, newIndex + 1).create()
                val newPage = doc.startPage(pageInfo)
                newPage.canvas.drawBitmap(finalBitmap, null, Rect(0, 0, width, height), null)
                doc.finishPage(newPage)
                finalBitmap.recycle()
            }

            FileOutputStream(outputFile).use { out ->
                doc.writeTo(out)
            }
        } finally {
            doc.close()
            renderer.close()
            pfd.close()
            tempFile.delete()
        }

        val finalSize = outputFile.length()
        val duration = System.currentTimeMillis() - startTime

        ProcessedFileResult(
            originalUri = originalUri,
            fileName = outputName,
            originalSizeBytes = originalSize,
            compressedSizeBytes = finalSize,
            outputFile = outputFile,
            mimeType = "application/pdf",
            processingTimeMs = duration
        )
    }
}
