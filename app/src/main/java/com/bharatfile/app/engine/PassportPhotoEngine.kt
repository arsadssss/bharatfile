package com.bharatfile.app.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.bharatfile.app.model.ImageFormat
import com.bharatfile.app.model.ProcessedFileResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class PassportPreset(
    val title: String,
    val aspectWidth: Int,
    val aspectHeight: Int,
    val targetWidthPx: Int,
    val targetHeightPx: Int,
    val description: String
) {
    INDIAN_PASSPORT(
        title = "Indian Passport / Visa (3.5 × 4.5 cm)",
        aspectWidth = 7,
        aspectHeight = 9,
        targetWidthPx = 413,
        targetHeightPx = 531,
        description = "Standard for Indian Passport, UPSC, SSC & Bank exams"
    ),
    STAMP_SIZE(
        title = "Stamp Size (2.5 × 3.0 cm)",
        aspectWidth = 5,
        aspectHeight = 6,
        targetWidthPx = 295,
        targetHeightPx = 354,
        description = "Common for college admissions and id cards"
    ),
    SQUARE_US_VISA(
        title = "Square 2 × 2 inch (5 × 5 cm)",
        aspectWidth = 1,
        aspectHeight = 1,
        targetWidthPx = 600,
        targetHeightPx = 600,
        description = "Standard US Visa / OCI document size"
    )
}

object PassportPhotoEngine {

    suspend fun cropToPreset(
        context: Context,
        inputUri: Uri,
        preset: PassportPreset
    ): ProcessedFileResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val originalFileName = FileHelper.getFileName(context, inputUri)
        val originalSize = FileHelper.getFileSize(context, inputUri)

        val stream = context.contentResolver.openInputStream(inputUri)
        val original = BitmapFactory.decodeStream(stream)
        stream?.close() ?: throw IllegalArgumentException("Cannot decode photo")

        // Center crop to aspect ratio
        val targetRatio = preset.aspectWidth.toFloat() / preset.aspectHeight.toFloat()
        val srcRatio = original.width.toFloat() / original.height.toFloat()

        val cropWidth: Int
        val cropHeight: Int
        val cropX: Int
        val cropY: Int

        if (srcRatio > targetRatio) {
            cropHeight = original.height
            cropWidth = (original.height * targetRatio).toInt()
            cropX = (original.width - cropWidth) / 2
            cropY = 0
        } else {
            cropWidth = original.width
            cropHeight = (original.width / targetRatio).toInt()
            cropX = 0
            cropY = (original.height - cropHeight) / 2
        }

        val cropped = Bitmap.createBitmap(original, cropX, cropY, cropWidth, cropHeight)
        val scaled = Bitmap.createScaledBitmap(cropped, preset.targetWidthPx, preset.targetHeightPx, true)

        if (cropped != original) original.recycle()
        if (scaled != cropped) cropped.recycle()

        val outputFile = File(context.cacheDir, "passport_${System.currentTimeMillis()}_photo.jpg")
        FileOutputStream(outputFile).use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        scaled.recycle()

        val finalSize = outputFile.length()
        val duration = System.currentTimeMillis() - startTime

        ProcessedFileResult(
            originalUri = inputUri,
            fileName = "passport_photo_${preset.name.lowercase()}.jpg",
            originalSizeBytes = originalSize,
            compressedSizeBytes = finalSize,
            outputFile = outputFile,
            mimeType = "image/jpeg",
            processingTimeMs = duration
        )
    }
}
