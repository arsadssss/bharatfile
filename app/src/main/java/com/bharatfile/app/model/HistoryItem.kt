package com.bharatfile.app.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HistoryItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val fileName: String,
    val originalSizeBytes: Long,
    val outputSizeBytes: Long,
    val operationType: String, // e.g. "PDF Compression", "Image Compression", "Image Resizing"
    val timestamp: Long = System.currentTimeMillis(),
    val filePath: String? = null,
    val mimeType: String = "*/*"
) {
    val savingsPercentage: Int
        get() {
            if (originalSizeBytes <= 0L) return 0
            val diff = originalSizeBytes - outputSizeBytes
            if (diff <= 0) return 0
            return ((diff.toDouble() / originalSizeBytes.toDouble()) * 100).toInt()
        }

    val formattedOriginalSize: String
        get() = ProcessedFileResult.formatFileSize(originalSizeBytes)

    val formattedOutputSize: String
        get() = ProcessedFileResult.formatFileSize(outputSizeBytes)

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
}
