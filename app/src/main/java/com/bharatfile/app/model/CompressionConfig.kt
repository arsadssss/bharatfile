package com.bharatfile.app.model

enum class CompressionMode(val displayName: String) {
    PERCENTAGE("Reduce by %"),
    TARGET_SIZE("Target final size")
}

enum class SizeUnit(val label: String, val multiplierBytes: Long) {
    KB("KB", 1024L),
    MB("MB", 1024L * 1024L);

    fun toBytes(value: Double): Long = (value * multiplierBytes).toLong()

    companion object {
        fun fromString(str: String): SizeUnit {
            return if (str.equals("MB", ignoreCase = true)) MB else KB
        }
    }
}

data class CompressionConfig(
    val mode: CompressionMode = CompressionMode.PERCENTAGE,
    val percentage: Int = 65, // 1 to 100
    val targetValue: String = "100",
    val targetUnit: SizeUnit = SizeUnit.KB
) {
    val targetBytes: Long?
        get() {
            if (mode != CompressionMode.TARGET_SIZE) return null
            val value = targetValue.toDoubleOrNull() ?: return null
            if (value <= 0) return null
            return targetUnit.toBytes(value)
        }

    fun validate(): String? {
        return when (mode) {
            CompressionMode.PERCENTAGE -> {
                if (percentage !in 1..100) "Compression percentage must be between 1% and 100%."
                else null
            }
            CompressionMode.TARGET_SIZE -> {
                val value = targetValue.toDoubleOrNull()
                if (value == null || value <= 0) "Target size must be a positive number greater than 0."
                else null
            }
        }
    }

    fun validateAgainstOriginal(originalSizeBytes: Long): String? {
        val err = validate()
        if (err != null) return err
        if (mode == CompressionMode.TARGET_SIZE) {
            val target = targetBytes ?: return null
            if (target >= originalSizeBytes) {
                return "Target size is larger than or equal to original file. Please choose a smaller target size to reduce."
            }
        }
        return null
    }

    companion object {
        val QUICK_PRESETS = listOf(
            Pair("50", SizeUnit.KB),
            Pair("100", SizeUnit.KB),
            Pair("200", SizeUnit.KB),
            Pair("500", SizeUnit.KB),
            Pair("1", SizeUnit.MB),
            Pair("2", SizeUnit.MB),
            Pair("5", SizeUnit.MB),
            Pair("10", SizeUnit.MB)
        )
    }
}

