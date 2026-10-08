package com.bharatfile.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Merge
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bharatfile.app.data.HistoryManager
import com.bharatfile.app.engine.FileHelper
import com.bharatfile.app.engine.PdfMerger
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
import kotlinx.coroutines.launch

@Composable
fun PdfMergeScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRoute: (String) -> Unit = {},
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = BharatFileThemeCustom.colors
    val scrollState = rememberScrollState()

    val selectedPdfs = remember { mutableStateListOf<Uri>() }
    var mergedName by remember { mutableStateOf("BharatFile_Merged.pdf") }
    var isProcessing by remember { mutableStateOf(false) }
    var progressStatus by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<ProcessedFileResult?>(null) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedPdfs.addAll(uris)
            result = null
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
                text = "Merge PDF Files",
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
                        .clickable { pickerLauncher.launch(arrayOf("application/pdf")) }
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Merge,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedPdfs.isEmpty()) "Select PDFs to Merge" else "Add More PDFs",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${selectedPdfs.size} PDFs selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }

                if (selectedPdfs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Document Merge Order",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textSecondary,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    selectedPdfs.forEachIndexed { index, uri ->
                        val fileName = FileHelper.getFileName(context, uri)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(InnerCardShape)
                                .background(colors.segmentedContainer.copy(alpha = 0.6f))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}.",
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = fileName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textPrimary,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { selectedPdfs.removeAt(index) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Remove",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = mergedName,
                        onValueChange = { mergedName = it },
                        label = { Text("Merged File Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (isProcessing) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = ElectricBlue)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = progressStatus, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                        }
                    } else {
                        GradientButton(
                            text = "Merge ${selectedPdfs.size} PDFs",
                            enabled = selectedPdfs.size >= 2,
                            onClick = {
                                scope.launch {
                                    isProcessing = true
                                    try {
                                        val validName = if (mergedName.endsWith(".pdf")) mergedName else "$mergedName.pdf"
                                        result = PdfMerger.mergePdfs(
                                            context = context,
                                            pdfUris = selectedPdfs.toList(),
                                            outputName = validName,
                                            onProgress = { _, status -> progressStatus = status }
                                        )
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Merge error: ${e.localizedMessage}")
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
                        text = "PDFs Merged Successfully!",
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
                        text = "Download Merged PDF",
                        onClick = {
                            FileHelper.saveToDownloads(context, res.outputFile, res.fileName, res.mimeType)
                            HistoryManager.getInstance(context).addItem(
                                HistoryItem(
                                    fileName = res.fileName,
                                    originalSizeBytes = res.originalSizeBytes,
                                    outputSizeBytes = res.compressedSizeBytes,
                                    operationType = "PDF Merge",
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
            currentRoute = Screen.PdfMerge.route,
            onNavigateToRoute = onNavigateToRoute
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

