package com.bharatfile.app.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.bharatfile.app.model.ProcessedFileResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ImageToPdfEngine {

    suspend fun createPdfFromImages(
        context: Context,
        imageUris: List<Uri>,
        outputFileName: String = "BharatFile_Document.pdf",
        marginPt: Float = 20f
    ): ProcessedFileResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        var totalOriginalSize = 0L
        imageUris.forEach { uri ->
            totalOriginalSize += FileHelper.getFileSize(context, uri)
        }

        // Standard A4 dimensions in PostScript points: 595 x 842
        val pageWidth = 595
        val pageHeight = 842

        val pdfDocument = PdfDocument()
        val outputFile = File(context.cacheDir, "img2pdf_${System.currentTimeMillis()}_$outputFileName")

        imageUris.forEachIndexed { index, uri ->
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap != null) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                val availableWidth = pageWidth - (marginPt * 2)
                val availableHeight = pageHeight - (marginPt * 2)

                val imgRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                val pageRatio = availableWidth / availableHeight

                val drawWidth: Float
                val drawHeight: Float
                if (imgRatio > pageRatio) {
                    drawWidth = availableWidth
                    drawHeight = availableWidth / imgRatio
                } else {
                    drawHeight = availableHeight
                    drawWidth = availableHeight * imgRatio
                }

                val left = marginPt + (availableWidth - drawWidth) / 2f
                val top = marginPt + (availableHeight - drawHeight) / 2f
                val destRect = RectF(left, top, left + drawWidth, top + drawHeight)

                canvas.drawBitmap(bitmap, null, destRect, Paint(Paint.FILTER_BITMAP_FLAG))
                pdfDocument.finishPage(page)
                bitmap.recycle()
            }
        }

        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        val finalSize = outputFile.length()
        val duration = System.currentTimeMillis() - startTime

        ProcessedFileResult(
            originalUri = imageUris.firstOrNull(),
            fileName = outputFileName,
            originalSizeBytes = totalOriginalSize,
            compressedSizeBytes = finalSize,
            outputFile = outputFile,
            mimeType = "application/pdf",
            processingTimeMs = duration
        )
    }
}
