package com.bharatfile.app.model

import android.net.Uri
import java.io.File
import java.text.DecimalFormat

data class ProcessedFileResult(
    val originalUri: Uri?,
    val fileName: String,
    val originalSizeBytes: Long,
    val compressedSizeBytes: Long,
    val outputFile: File,
    val mimeType: String,
    val processingTimeMs: Long = 0L,
    val targetSizeBytes: Long? = null,
    val isTargetMode: Boolean = false
) {
    val savingsPercentage: Int
        get() {
            if (originalSizeBytes <= 0L) return 0
            val diff = originalSizeBytes - compressedSizeBytes
            if (diff <= 0) return 0
            return ((diff.toDouble() / originalSizeBytes.toDouble()) * 100).toInt()
        }

    val formattedOriginalSize: String
        get() = formatFileSize(originalSizeBytes)

    val formattedCompressedSize: String
        get() = formatFileSize(compressedSizeBytes)

    val formattedTargetSize: String?
        get() = targetSizeBytes?.let { formatFileSize(it) }

    val savingsPercentageString: String
        get() = "$savingsPercentage% smaller"

    val deltaFromTargetBytes: Long?
        get() = targetSizeBytes?.let { compressedSizeBytes - it }

    val targetComparisonString: String?
        get() {
            val target = targetSizeBytes ?: return null
            val actual = compressedSizeBytes
            val diff = actual - target
            return if (diff <= 0) {
                val under = formatFileSize(-diff)
                "Target: ${formatFileSize(target)} | Actual: ${formatFileSize(actual)} ($under under target)"
            } else {
                val over = formatFileSize(diff)
                "Target: ${formatFileSize(target)} | Actual: ${formatFileSize(actual)} ($over over target)"
            }
        }

    companion object {
        fun formatFileSize(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB")
            var size = bytes.toDouble()
            var unitIndex = 0
            while (size >= 1024 && unitIndex < units.size - 1) {
                size /= 1024
                unitIndex++
            }
            val df = DecimalFormat(if (unitIndex == 0) "#" else "#.#")
            return "${df.format(size)} ${units[unitIndex]}"
        }
    }
}

