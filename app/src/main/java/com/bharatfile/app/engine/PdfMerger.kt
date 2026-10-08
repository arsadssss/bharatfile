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

object PdfMerger {

    suspend fun mergePdfs(
        context: Context,
        pdfUris: List<Uri>,
        outputName: String = "BharatFile_Merged.pdf",
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): ProcessedFileResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        var totalOriginalSize = 0L
        pdfUris.forEach { uri ->
            totalOriginalSize += FileHelper.getFileSize(context, uri)
        }

        val mergedDoc = PdfDocument()
        val outputFile = File(context.cacheDir, "merged_${System.currentTimeMillis()}_$outputName")
        var overallPageIndex = 0

        pdfUris.forEachIndexed { docIndex, uri ->
            val progress = (docIndex.toFloat() / pdfUris.size.toFloat())
            onProgress(progress, "Merging document ${docIndex + 1} of ${pdfUris.size}…")

            val tempFile = FileHelper.copyUriToCacheFile(context, uri, "merge_in_")
            val pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)

            try {
                for (p in 0 until renderer.pageCount) {
                    val page = renderer.openPage(p)
                    val width = page.width
                    val height = page.height

                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    canvas.drawColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                    page.close()

                    val pageInfo = PdfDocument.PageInfo.Builder(width, height, ++overallPageIndex).create()
                    val newPage = mergedDoc.startPage(pageInfo)
                    newPage.canvas.drawBitmap(bitmap, null, Rect(0, 0, width, height), null)
                    mergedDoc.finishPage(newPage)
                    bitmap.recycle()
                }
            } finally {
                renderer.close()
                pfd.close()
                tempFile.delete()
            }
        }

        onProgress(0.95f, "Finalizing merged document…")
        FileOutputStream(outputFile).use { out ->
            mergedDoc.writeTo(out)
        }
        mergedDoc.close()

        val finalSize = outputFile.length()
        val duration = System.currentTimeMillis() - startTime

        ProcessedFileResult(
            originalUri = pdfUris.firstOrNull(),
            fileName = outputName,
            originalSizeBytes = totalOriginalSize,
            compressedSizeBytes = finalSize,
            outputFile = outputFile,
            mimeType = "application/pdf",
            processingTimeMs = duration
        )
    }
}
