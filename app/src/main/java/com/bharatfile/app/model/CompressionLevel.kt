package com.bharatfile.app.model

enum class CompressionLevel(
    val title: String,
    val description: String,
    val qualityFactor: Float, // 0.0 - 1.0 (higher means better quality)
    val dpiReductionFactor: Float
) {
    LOW(
        title = "Low",
        description = "Slight reduction, highest visual clarity",
        qualityFactor = 0.85f,
        dpiReductionFactor = 1.0f
    ),
    MEDIUM(
        title = "Medium",
        description = "Optimal balance between size and quality",
        qualityFactor = 0.60f,
        dpiReductionFactor = 0.80f
    ),
    HIGH(
        title = "High",
        description = "Maximum size reduction for portal uploads",
        qualityFactor = 0.35f,
        dpiReductionFactor = 0.60f
    );

    companion object {
        fun fromProgress(progress: Float): CompressionLevel {
            return when {
                progress < 0.33f -> LOW
                progress < 0.67f -> MEDIUM
                else -> HIGH
            }
        }
    }
}
