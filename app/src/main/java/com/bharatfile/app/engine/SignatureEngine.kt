package com.bharatfile.app.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.bharatfile.app.model.ProcessedFileResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object SignatureEngine {

    suspend fun saveSignature(
        context: Context,
        signatureBitmap: Bitmap,
        transparentBg: Boolean = true,
        outputName: String = "BharatFile_Signature.png"
    ): ProcessedFileResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val outputFile = File(context.cacheDir, "sig_${System.currentTimeMillis()}_$outputName")

        FileOutputStream(outputFile).use { out ->
            signatureBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val size = outputFile.length()
        val duration = System.currentTimeMillis() - startTime

        ProcessedFileResult(
            originalUri = null,
            fileName = outputName,
            originalSizeBytes = size,
            compressedSizeBytes = size,
            outputFile = outputFile,
            mimeType = "image/png",
            processingTimeMs = duration
        )
    }
}
