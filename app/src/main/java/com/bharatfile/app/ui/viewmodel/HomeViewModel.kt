package com.bharatfile.app.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bharatfile.app.data.HistoryManager
import com.bharatfile.app.data.LocalAuthRepository
import com.bharatfile.app.data.PreferencesManager
import com.bharatfile.app.engine.FileCleaner
import com.bharatfile.app.engine.FileHelper
import com.bharatfile.app.engine.ImageCompressor
import com.bharatfile.app.engine.PdfCompressor
import com.bharatfile.app.model.CompressionConfig
import com.bharatfile.app.model.CompressionLevel
import com.bharatfile.app.model.DocumentType
import com.bharatfile.app.model.HistoryItem
import com.bharatfile.app.model.ProcessedFileResult
import com.bharatfile.app.model.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferencesManager.getInstance(application)
    private val historyManager = HistoryManager.getInstance(application)
    private val authRepo = LocalAuthRepository.getInstance(application)

    private val _documentType = MutableStateFlow(DocumentType.PDF)
    val documentType: StateFlow<DocumentType> = _documentType.asStateFlow()

    private val _compressionConfig = MutableStateFlow(
        prefs.getConfigForDocumentType(DocumentType.PDF)
    )
    val compressionConfig: StateFlow<CompressionConfig> = _compressionConfig.asStateFlow()

    // Backward compatibility for components expecting CompressionLevel
    private val _compressionLevel = MutableStateFlow(CompressionLevel.MEDIUM)
    val compressionLevel: StateFlow<CompressionLevel> = _compressionLevel.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Empty)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private var compressionJob: Job? = null

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setDocumentType(type: DocumentType) {
        if (_documentType.value != type) {
            _documentType.value = type
            _compressionConfig.value = prefs.getConfigForDocumentType(type)
            _uiState.value = UiState.Empty
        }
    }

    fun setCompressionConfig(config: CompressionConfig) {
        _compressionConfig.value = config
        // Keep level in sync for backward compatibility
        _compressionLevel.value = when {
            config.percentage < 40 -> CompressionLevel.LOW
            config.percentage < 75 -> CompressionLevel.MEDIUM
            else -> CompressionLevel.HIGH
        }
    }

    fun setCompressionPercentage(percentage: Int) {
        val updated = _compressionConfig.value.copy(percentage = percentage)
        setCompressionConfig(updated)
    }

    fun saveCurrentConfigAsDefault() {
        prefs.saveConfigAsDefault(_documentType.value, _compressionConfig.value)
        _userMessage.value = "Default compression preference saved!"
    }

    fun setCompressionLevel(level: CompressionLevel) {
        _compressionLevel.value = level
        val p = when (level) {
            CompressionLevel.LOW -> 30
            CompressionLevel.MEDIUM -> 65
            CompressionLevel.HIGH -> 88
        }
        setCompressionPercentage(p)
    }

    fun onFileSelected(uri: Uri) {
        val context = getApplication<Application>()
        val fileName = FileHelper.getFileName(context, uri)
        val fileSize = FileHelper.getFileSize(context, uri)

        if (fileSize <= 0) {
            _uiState.value = UiState.Error("Selected file is empty or cannot be accessed.")
            return
        }

        _uiState.value = UiState.Selected(
            uri = uri,
            fileName = fileName,
            originalSizeBytes = fileSize
        )
    }

    fun startCompression() {
        val currentState = _uiState.value
        if (currentState !is UiState.Selected) return

        val context = getApplication<Application>()
        val uri = currentState.uri
        val fileName = currentState.fileName
        val originalSize = currentState.originalSizeBytes
        val config = _compressionConfig.value

        // Validate config
        val validationErr = config.validate()
        if (validationErr != null) {
            _userMessage.value = validationErr
            return
        }

        val estimatedSize = if (_documentType.value == DocumentType.PDF) {
            PdfCompressor.estimateCompressedSize(originalSize, config)
        } else {
            ImageCompressor.estimateCompressedSize(originalSize, config)
        }

        compressionJob?.cancel()
        compressionJob = viewModelScope.launch {
            _uiState.value = UiState.Processing(
                fileName = fileName,
                originalSizeBytes = originalSize,
                estimatedSizeBytes = estimatedSize,
                progress = 0.05f,
                statusMessage = "Analyzing file structure…"
            )

            try {
                val result: ProcessedFileResult = if (_documentType.value == DocumentType.PDF) {
                    PdfCompressor.compressWithConfig(
                        context = context,
                        inputUri = uri,
                        config = config,
                        onProgress = { progress, msg ->
                            _uiState.value = UiState.Processing(
                                fileName = fileName,
                                originalSizeBytes = originalSize,
                                estimatedSizeBytes = estimatedSize,
                                progress = progress,
                                statusMessage = msg
                            )
                        }
                    )
                } else {
                    ImageCompressor.compressWithConfig(
                        context = context,
                        inputUri = uri,
                        config = config,
                        onProgress = { progress, msg ->
                            _uiState.value = UiState.Processing(
                                fileName = fileName,
                                originalSizeBytes = originalSize,
                                estimatedSizeBytes = estimatedSize,
                                progress = progress,
                                statusMessage = msg
                            )
                        }
                    )
                }

                // Save to history & update profile stats
                val opName = if (_documentType.value == DocumentType.PDF) "PDF Compression" else "Image Compression"
                historyManager.addItem(
                    HistoryItem(
                        fileName = result.fileName,
                        originalSizeBytes = result.originalSizeBytes,
                        outputSizeBytes = result.compressedSizeBytes,
                        operationType = opName,
                        filePath = result.outputFile.absolutePath,
                        mimeType = result.mimeType
                    )
                )

                val diff = result.originalSizeBytes - result.compressedSizeBytes
                if (diff > 0) {
                    authRepo.recordProcessedFile(diff)
                }

                _uiState.value = UiState.Success(result)
            } catch (e: CancellationException) {
                // User cancelled the operation
                FileCleaner.clearCache(context)
                _uiState.value = UiState.Empty
                _userMessage.value = "Compression cancelled"
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    e.localizedMessage ?: "Unable to process file. Please ensure it is a valid format."
                )
            }
        }
    }

    fun cancelOperation() {
        compressionJob?.cancel()
        val context = getApplication<Application>()
        FileCleaner.clearCache(context)
        _uiState.value = UiState.Empty
        _userMessage.value = "Operation cancelled"
    }

    fun compressAnother() {
        _uiState.value = UiState.Empty
    }

    fun saveResult(result: ProcessedFileResult) {
        val context = getApplication<Application>()
        val savedUri = FileHelper.saveToDownloads(
            context = context,
            sourceFile = result.outputFile,
            displayName = result.fileName,
            mimeType = result.mimeType
        )
        if (savedUri != null) {
            _userMessage.value = "Saved to Downloads/BharatFile!"
        } else {
            _userMessage.value = "Failed to save file."
        }
    }

    fun shareResult(result: ProcessedFileResult) {
        val context = getApplication<Application>()
        FileHelper.shareFile(context, result.outputFile, result.mimeType)
    }

    fun openResult(result: ProcessedFileResult) {
        val context = getApplication<Application>()
        FileHelper.openFile(context, result.outputFile, result.mimeType)
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
