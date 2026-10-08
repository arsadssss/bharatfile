package com.bharatfile.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bharatfile.app.R
import com.bharatfile.app.model.CompressionConfig
import com.bharatfile.app.model.CompressionMode
import com.bharatfile.app.navigation.Screen
import com.bharatfile.app.model.DocumentType
import com.bharatfile.app.model.ProcessedFileResult
import com.bharatfile.app.model.UiState
import com.bharatfile.app.theme.BharatFileThemeCustom
import com.bharatfile.app.theme.ElectricBlue
import com.bharatfile.app.theme.InnerCardShape
import com.bharatfile.app.theme.PillShape
import com.bharatfile.app.theme.SuccessGreen
import com.bharatfile.app.ui.components.BharatFileHeader
import com.bharatfile.app.ui.components.CustomCompressionSlider
import com.bharatfile.app.ui.components.GlassCard
import com.bharatfile.app.ui.components.GradientButton
import com.bharatfile.app.ui.components.GradientProgressBar
import com.bharatfile.app.ui.components.QuickToolsRow
import com.bharatfile.app.ui.components.RecommendedToolsSection
import com.bharatfile.app.ui.components.SegmentedControl
import com.bharatfile.app.ui.components.SizeComparisonCard
import com.bharatfile.app.ui.components.TargetSizeSelector
import com.bharatfile.app.ui.components.illustrations.FloatingFolderIllustration
import com.bharatfile.app.ui.components.illustrations.ProcessingIllustration
import com.bharatfile.app.ui.components.illustrations.SuccessIllustration
import com.bharatfile.app.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToImages: () -> Unit,
    onNavigateToResize: () -> Unit,
    onNavigateToConvert: () -> Unit,
    onNavigateToRoute: (String) -> Unit = {},
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val colors = BharatFileThemeCustom.colors
    val scrollState = rememberScrollState()

    val documentType by viewModel.documentType.collectAsState()
    val compressionConfig by viewModel.compressionConfig.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    // PDF File Picker
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.onFileSelected(it) }
    }

    // Image File Picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.onFileSelected(it) }
    }

    val onPickFile = {
        if (documentType == DocumentType.PDF) {
            pdfPickerLauncher.launch(arrayOf("application/pdf"))
        } else {
            imagePickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        // Header
        BharatFileHeader(
            isDarkMode = isDarkMode,
            onToggleDarkMode = { viewModel.toggleDarkMode() },
            onOpenMenu = onOpenDrawer
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Segmented Control [ PDF ] [ Image ]
        SegmentedControl(
            selectedType = documentType,
            onTypeSelected = { viewModel.setDocumentType(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // State Machine rendering:
        // Screen 1: Empty / Selected File with continuous slider & target size selector
        // Screen 2: Processing State with live cancellation button
        // Screen 3: Success State with delta comparison & recommended tools
        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "screenStateTransition"
        ) { state ->
            when (state) {
                is UiState.Empty, is UiState.Selected -> {
                    InitialUploadStateContent(
                        documentType = documentType,
                        compressionConfig = compressionConfig,
                        selectedFile = state as? UiState.Selected,
                        onPickFile = onPickFile,
                        onCompress = { viewModel.startCompression() },
                        onCancelSelected = { viewModel.cancelOperation() },
                        onConfigChange = { viewModel.setCompressionConfig(it) },
                        onPercentageChange = { viewModel.setCompressionPercentage(it) },
                        onSaveAsDefault = { viewModel.saveCurrentConfigAsDefault() }
                    )
                }

                is UiState.Processing -> {
                    ProcessingStateContent(
                        processingState = state,
                        onCancel = { viewModel.cancelOperation() },
                        documentType = documentType
                    )
                }

                is UiState.Success -> {
                    SuccessStateContent(
                        result = state.result,
                        onDownload = { viewModel.saveResult(state.result) },
                        onShare = { viewModel.shareResult(state.result) },
                        onOpen = { viewModel.openResult(state.result) },
                        onCompressAnother = { viewModel.compressAnother() },
                        onNavigateToImages = onNavigateToImages,
                        onNavigateToResize = onNavigateToResize,
                        onNavigateToConvert = onNavigateToConvert,
                        onNavigateToRoute = onNavigateToRoute,
                        documentType = documentType
                    )
                }

                is UiState.Error -> {
                    ErrorStateContent(
                        errorMessage = state.message,
                        onRetry = onPickFile,
                        onReset = { viewModel.compressAnother() }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun InitialUploadStateContent(
    documentType: DocumentType,
    compressionConfig: CompressionConfig,
    selectedFile: UiState.Selected?,
    onPickFile: () -> Unit,
    onCompress: () -> Unit,
    onCancelSelected: () -> Unit,
    onConfigChange: (CompressionConfig) -> Unit,
    onPercentageChange: (Int) -> Unit,
    onSaveAsDefault: () -> Unit
) {
    val colors = BharatFileThemeCustom.colors
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main Glass Upload Card with overlapping 3D illustration (Screen 1)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            // Background White/Glass Upload Card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(38.dp))

                    if (selectedFile == null) {
                        // Dashed Inner Container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(InnerCardShape)
                                .background(colors.segmentedContainer.copy(alpha = 0.5f))
                                .border(
                                    width = 1.2.dp,
                                    color = colors.dashedBorder,
                                    shape = InnerCardShape
                                )
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = onPickFile
                                )
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(ElectricBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CloudUpload,
                                        contentDescription = "Upload",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = if (documentType == DocumentType.PDF) "Select PDF File" else "Select Image File",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    ),
                                    color = colors.textPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // "Choose File" Pill Button
                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(colors.cardSurface)
                                .border(width = 1.dp, color = colors.cardBorder, shape = PillShape)
                                .clickable(onClick = onPickFile)
                                .padding(horizontal = 22.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.FolderOpen,
                                    contentDescription = null,
                                    tint = colors.textPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Choose File",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
                                    ),
                                    color = colors.textPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(0.6f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Divider(modifier = Modifier.weight(1f), color = colors.cardBorder)
                            Text(
                                text = "  or  ",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = colors.textSecondary
                            )
                            Divider(modifier = Modifier.weight(1f), color = colors.cardBorder)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Drag & drop here",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = colors.textSecondary
                        )
                    } else {
                        // File Selected State inside card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(InnerCardShape)
                                .background(colors.segmentedContainer)
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(
                                    id = if (documentType == DocumentType.PDF) R.drawable.ic_pdf_badge else R.drawable.ic_pdf_green_badge
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedFile.fileName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = selectedFile.formattedSize,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                            IconButton(onClick = onCancelSelected, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Remove file",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Primary action to compress
                        GradientButton(
                            text = if (documentType == DocumentType.PDF) "Compress PDF" else "Compress Image",
                            onClick = onCompress
                        )
                    }
                }
            }

            // Floating 3D Folder Illustration
            FloatingFolderIllustration(
                size = 92.dp,
                documentType = documentType,
                modifier = Modifier.offset(y = (-6).dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Target Size Mode Selector (Toggle between "Reduce by %" and "Target final size")
        TargetSizeSelector(
            config = compressionConfig,
            originalSizeBytes = selectedFile?.originalSizeBytes,
            onConfigChange = onConfigChange
        )

        // Continuous Compression Slider (1% to 100%) when in percentage mode
        if (compressionConfig.mode == CompressionMode.PERCENTAGE) {
            Spacer(modifier = Modifier.height(14.dp))
            CustomCompressionSlider(
                percentage = compressionConfig.percentage,
                onPercentageChange = onPercentageChange,
                onSaveAsDefault = onSaveAsDefault
            )
        }
    }
}

@Composable
private fun ProcessingStateContent(
    processingState: UiState.Processing,
    onCancel: () -> Unit,
    documentType: DocumentType
) {
    val colors = BharatFileThemeCustom.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Central 3D Processing Document (Screen 2)
        ProcessingIllustration(size = 145.dp)

        Spacer(modifier = Modifier.height(10.dp))

        // File info
        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = processingState.fileName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = colors.textPrimary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(colors.segmentedContainer)
                            .clickable(onClick = onCancel),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Cancel",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = processingState.formattedOriginalSize,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = colors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Progress Section: "Compressing..." left, "60%" right + Progress bar
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = processingState.statusMessage,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    ),
                    color = colors.textPrimary
                )
                Text(
                    text = "${processingState.progressPercentage}%",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            GradientProgressBar(progress = processingState.progress)
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Cancel Processing button
        Box(
            modifier = Modifier
                .clip(PillShape)
                .background(colors.segmentedContainer)
                .border(1.dp, colors.cardBorder, PillShape)
                .clickable(onClick = onCancel)
                .padding(horizontal = 20.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Cancel,
                    contentDescription = "Cancel",
                    tint = colors.error,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Cancel Processing",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    ),
                    color = colors.error
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Comparison Preview Card (Screen 2)
        SizeComparisonCard(
            originalSize = processingState.formattedOriginalSize,
            compressedSize = processingState.formattedEstimatedSize,
            documentType = documentType
        )
    }
}

@Composable
private fun SuccessStateContent(
    result: ProcessedFileResult,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onOpen: () -> Unit,
    onCompressAnother: () -> Unit,
    onNavigateToImages: () -> Unit,
    onNavigateToResize: () -> Unit,
    onNavigateToConvert: () -> Unit,
    onNavigateToRoute: (String) -> Unit,
    documentType: DocumentType
) {
    val colors = BharatFileThemeCustom.colors
    var showDropdownMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Central 3D Success Document with Green Halo (Screen 3)
        SuccessIllustration(size = 145.dp)

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Compression Complete",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Result Card: PDF icon + document.pdf + 2.4 MB → 890 KB (in green) + Delta info
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(
                            id = if (documentType == DocumentType.PDF) R.drawable.ic_pdf_badge else R.drawable.ic_pdf_green_badge
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(38.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = result.fileName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = colors.textPrimary,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${result.formattedOriginalSize}  →  ",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = colors.textSecondary
                            )
                            Text(
                                text = result.formattedCompressedSize,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                color = SuccessGreen
                            )
                            if (result.savingsPercentage > 0) {
                                Text(
                                    text = " (${result.savingsPercentageString})",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = SuccessGreen
                                )
                            }
                        }
                    }
                }

                // If Target mode was used, display exact Target vs Actual comparison delta!
                if (result.targetComparisonString != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricBlue.copy(alpha = 0.10f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = result.targetComparisonString!!,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            color = ElectricBlue
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Primary Action: Download Button with Dropdown options
        Box(modifier = Modifier.fillMaxWidth()) {
            GradientButton(
                text = "Download",
                onClick = onDownload,
                showDropdownArrow = true,
                modifier = Modifier.fillMaxWidth()
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(PillShape)
                    .clickable { showDropdownMenu = true }
            )

            DropdownMenu(
                expanded = showDropdownMenu,
                onDismissRequest = { showDropdownMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Save to Downloads") },
                    onClick = {
                        showDropdownMenu = false
                        onDownload()
                    },
                    leadingIcon = { Icon(Icons.Outlined.CloudUpload, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Open File") },
                    onClick = {
                        showDropdownMenu = false
                        onOpen()
                    },
                    leadingIcon = { Icon(Icons.Outlined.FileOpen, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Share File") },
                    onClick = {
                        showDropdownMenu = false
                        onShare()
                    },
                    leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Secondary Action: "Compress Another" button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(PillShape)
                .background(colors.cardSurface)
                .border(width = 1.dp, color = colors.cardBorder, shape = PillShape)
                .clickable(onClick = onCompressAnother)
                .padding(vertical = 13.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Compress Another",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    ),
                    color = colors.textPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Context-aware Recommended Tools Section
        RecommendedToolsSection(
            currentRoute = if (documentType == DocumentType.PDF) Screen.Home.route else "compress_image",
            onNavigateToRoute = onNavigateToRoute
        )
    }
}

@Composable
private fun ErrorStateContent(
    errorMessage: String,
    onRetry: () -> Unit,
    onReset: () -> Unit
) {
    val colors = BharatFileThemeCustom.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Unable to Process File",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(PillShape)
                            .background(colors.cardBorder)
                            .clickable(onClick = onReset)
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Cancel", color = colors.textPrimary, fontSize = 13.sp)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(PillShape)
                            .background(ElectricBlue)
                            .clickable(onClick = onRetry)
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Try Another", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
