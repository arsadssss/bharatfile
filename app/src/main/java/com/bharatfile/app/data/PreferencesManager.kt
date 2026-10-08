package com.bharatfile.app.data

import android.content.Context
import android.content.SharedPreferences
import com.bharatfile.app.model.CompressionConfig
import com.bharatfile.app.model.CompressionMode
import com.bharatfile.app.model.DocumentType
import com.bharatfile.app.model.SizeUnit

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var defaultPdfPercentage: Int
        get() = prefs.getInt(KEY_DEFAULT_PDF_PERCENTAGE, 65)
        set(value) = prefs.edit().putInt(KEY_DEFAULT_PDF_PERCENTAGE, value.coerceIn(1, 100)).apply()

    var defaultImagePercentage: Int
        get() = prefs.getInt(KEY_DEFAULT_IMAGE_PERCENTAGE, 70)
        set(value) = prefs.edit().putInt(KEY_DEFAULT_IMAGE_PERCENTAGE, value.coerceIn(1, 100)).apply()

    var defaultMode: CompressionMode
        get() {
            val name = prefs.getString(KEY_DEFAULT_MODE, CompressionMode.PERCENTAGE.name)
            return try {
                CompressionMode.valueOf(name ?: CompressionMode.PERCENTAGE.name)
            } catch (_: Exception) {
                CompressionMode.PERCENTAGE
            }
        }
        set(value) = prefs.edit().putString(KEY_DEFAULT_MODE, value.name).apply()

    var defaultTargetValue: String
        get() = prefs.getString(KEY_DEFAULT_TARGET_VALUE, "100") ?: "100"
        set(value) = prefs.edit().putString(KEY_DEFAULT_TARGET_VALUE, value).apply()

    var defaultTargetUnit: SizeUnit
        get() {
            val name = prefs.getString(KEY_DEFAULT_TARGET_UNIT, SizeUnit.KB.name)
            return try {
                SizeUnit.valueOf(name ?: SizeUnit.KB.name)
            } catch (_: Exception) {
                SizeUnit.KB
            }
        }
        set(value) = prefs.edit().putString(KEY_DEFAULT_TARGET_UNIT, value.name).apply()

    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM"
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value).apply()

    fun getConfigForDocumentType(type: DocumentType): CompressionConfig {
        val percentage = when (type) {
            DocumentType.PDF -> defaultPdfPercentage
            DocumentType.IMAGE -> defaultImagePercentage
        }
        return CompressionConfig(
            mode = defaultMode,
            percentage = percentage,
            targetValue = defaultTargetValue,
            targetUnit = defaultTargetUnit
        )
    }

    fun saveConfigAsDefault(type: DocumentType, config: CompressionConfig) {
        when (type) {
            DocumentType.PDF -> defaultPdfPercentage = config.percentage
            DocumentType.IMAGE -> defaultImagePercentage = config.percentage
        }
        defaultMode = config.mode
        defaultTargetValue = config.targetValue
        defaultTargetUnit = config.targetUnit
    }

    companion object {
        private const val PREFS_NAME = "bharatfile_preferences"
        private const val KEY_DEFAULT_PDF_PERCENTAGE = "default_pdf_percentage"
        private const val KEY_DEFAULT_IMAGE_PERCENTAGE = "default_image_percentage"
        private const val KEY_DEFAULT_MODE = "default_mode"
        private const val KEY_DEFAULT_TARGET_VALUE = "default_target_value"
        private const val KEY_DEFAULT_TARGET_UNIT = "default_target_unit"
        private const val KEY_THEME_MODE = "theme_mode"

        @Volatile
        private var instance: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: PreferencesManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
