package com.bharatfile.app.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Filter
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bharatfile.app.data.HistoryManager
import com.bharatfile.app.engine.DocumentScannerEngine
import com.bharatfile.app.engine.FileHelper
import com.bharatfile.app.engine.ScanFilter
import com.bharatfile.app.model.HistoryItem
import com.bharatfile.app.model.ProcessedFileResult
import com.bharatfile.app.navigation.Screen
import com.bharatfile.app.theme.BharatFileThemeCustom
import com.bharatfile.app.theme.ElectricBlue
import com.bharatfile.app.theme.InnerCardShape
import com.bharatfile.app.theme.PillShape
import com.bharatfile.app.theme.SuccessGreen
import com.bharatfile.app.ui.components.GlassCard
import com.bharatfile.app.ui.components.GradientButton
import com.bharatfile.app.ui.components.RecommendedToolsSection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DocumentScannerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRoute: (String) -> Unit = {},
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = BharatFileThemeCustom.colors
    val scrollState = rememberScrollState()

    val rawBitmaps = remember { mutableStateListOf<Bitmap>() }
    var selectedFilter by remember { mutableStateOf(ScanFilter.MAGIC_COLOR) }
    var pdfTitle by remember { mutableStateOf("BharatFile_Scan.pdf") }
    var isProcessing by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<ProcessedFileResult?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            scope.launch {
                withContext(Dispatchers.IO) {
                    uris.forEach { uri ->
                        val stream = context.contentResolver.openInputStream(uri)
                        val bmp = BitmapFactory.decodeStream(stream)
                        stream?.close()
                        if (bmp != null) {
                            rawBitmaps.add(bmp)
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Document Scanner",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = colors.textPrimary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(InnerCardShape)
                        .background(colors.segmentedContainer)
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (rawBitmaps.isEmpty()) "Scan / Select Document Pages" else "Add More Pages",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${rawBitmaps.size} pages captured",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }

                if (rawBitmaps.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Document Filter",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            ScanFilter.MAGIC_COLOR to "Magic Color",
                            ScanFilter.BLACK_AND_WHITE to "B & W",
                            ScanFilter.GRAYSCALE to "Grayscale",
                            ScanFilter.ORIGINAL to "Original"
                        ).forEach { (filter, label) ->
                            val isSelected = selectedFilter == filter
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) ElectricBlue else colors.segmentedContainer)
                                    .clickable { selectedFilter = filter }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else colors.textPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Filtered Pages Preview",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textSecondary,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(rawBitmaps) { index, bmp ->
                            val filteredBmp = remember(bmp, selectedFilter) {
                                DocumentScannerEngine.applyFilter(bmp, selectedFilter)
                            }
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                            ) {
                                Image(
                                    bitmap = filteredBmp.asImageBitmap(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = "p.${index + 1}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(20.dp)
                                        .clip(PillShape)
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .clickable { rawBitmaps.removeAt(index) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Remove",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = pdfTitle,
                        onValueChange = { pdfTitle = it },
                        label = { Text("Scanned PDF Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (isProcessing) {
                        CircularProgressIndicator(color = ElectricBlue)
                    } else {
                        GradientButton(
                            text = "Save Scanned PDF (${rawBitmaps.size} Pages)",
                            onClick = {
                                scope.launch {
                                    isProcessing = true
                                    try {
                                        val processedBitmaps = rawBitmaps.map {
                                            DocumentScannerEngine.applyFilter(it, selectedFilter)
                                        }
                                        val validName = if (pdfTitle.endsWith(".pdf")) pdfTitle else "$pdfTitle.pdf"
                                        result = DocumentScannerEngine.saveScannedPagesToPdf(
                                            context = context,
                                            pageBitmaps = processedBitmaps,
                                            outputName = validName
                                        )
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Scan export failed: ${e.localizedMessage}")
                                    } finally {
                                        isProcessing = false
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        result?.let { res ->
            Spacer(modifier = Modifier.height(20.dp))
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Scanned PDF Exported!",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = SuccessGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${res.fileName} (${res.formattedCompressedSize})",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    GradientButton(
                        text = "Download Scanned PDF",
                        onClick = {
                            FileHelper.saveToDownloads(context, res.outputFile, res.fileName, res.mimeType)
                            HistoryManager.getInstance(context).addItem(
                                HistoryItem(
                                    fileName = res.fileName,
                                    originalSizeBytes = res.originalSizeBytes,
                                    outputSizeBytes = res.compressedSizeBytes,
                                    operationType = "Document Scanner",
                                    filePath = res.outputFile.absolutePath,
                                    mimeType = res.mimeType
                                )
                            )
                            scope.launch { snackbarHostState.showSnackbar("Saved to Downloads/BharatFile!") }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Context-aware Recommended Tools Section
        RecommendedToolsSection(
            currentRoute = Screen.Scanner.route,
            onNavigateToRoute = onNavigateToRoute
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

