package com.bharatfile.app.model

import android.net.Uri

sealed interface UiState {
    object Empty : UiState

    data class Selected(
        val uri: Uri,
        val fileName: String,
        val originalSizeBytes: Long
    ) : UiState {
        val formattedSize: String
            get() = ProcessedFileResult.formatFileSize(originalSizeBytes)
    }

    data class Processing(
        val fileName: String,
        val originalSizeBytes: Long,
        val estimatedSizeBytes: Long,
        val progress: Float, // 0.0 to 1.0
        val statusMessage: String = "Compressing…"
    ) : UiState {
        val progressPercentage: Int
            get() = (progress * 100).coerceIn(0f, 100f).toInt()

        val formattedOriginalSize: String
            get() = ProcessedFileResult.formatFileSize(originalSizeBytes)

        val formattedEstimatedSize: String
            get() = ProcessedFileResult.formatFileSize(estimatedSizeBytes)
    }

    data class Success(
        val result: ProcessedFileResult
    ) : UiState

    data class Error(
        val message: String
    ) : UiState
}
