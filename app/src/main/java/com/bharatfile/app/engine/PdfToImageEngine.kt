package com.bharatfile.app.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.bharatfile.app.model.ImageFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object PdfToImageEngine {

    suspend fun extractPagesAsImages(
        context: Context,
        pdfUri: Uri,
        format: ImageFormat = ImageFormat.JPEG,
        dpiFactor: Float = 2.0f, // 2x for sharp print/screen quality
        quality: Int = 90
    ): List<File> = withContext(Dispatchers.IO) {
        val tempInputFile = FileHelper.copyUriToCacheFile(context, pdfUri, "pdf2img_")
        val pfd = ParcelFileDescriptor.open(tempInputFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        val extractedFiles = mutableListOf<File>()

        try {
            val baseName = FileHelper.getFileName(context, pdfUri).substringBeforeLast('.')
            val ext = format.extension

            for (i in 0 until renderer.pageCount) {
                val page = renderer.openPage(i)
                val width = (page.width * dpiFactor).toInt()
                val height = (page.height * dpiFactor).toInt()

                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                page.close()

                val pageFile = File(context.cacheDir, "${baseName}_page_${i + 1}.$ext")
                FileOutputStream(pageFile).use { out ->
                    bitmap.compress(format.compressFormat, quality, out)
                }
                bitmap.recycle()
                extractedFiles.add(pageFile)
            }
        } finally {
            renderer.close()
            pfd.close()
            tempInputFile.delete()
        }

        extractedFiles
    }
}
