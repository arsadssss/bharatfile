package com.bharatfile.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PictureAsPdf
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bharatfile.app.data.HistoryManager
import com.bharatfile.app.engine.FileHelper
import com.bharatfile.app.engine.ImageToPdfEngine
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
fun ImageToPdfScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRoute: (String) -> Unit = {},
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = BharatFileThemeCustom.colors
    val scrollState = rememberScrollState()

    val selectedImages = remember { mutableStateListOf<Uri>() }
    var pdfTitle by remember { mutableStateOf("BharatFile_Document.pdf") }
    var isProcessing by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<ProcessedFileResult?>(null) }

    val multiPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedImages.addAll(uris)
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
                text = "Images to PDF",
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
                // Add Images Button / Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(InnerCardShape)
                        .background(colors.segmentedContainer)
                        .clickable {
                            multiPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.AddPhotoAlternate,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedImages.isEmpty()) "Select Photos to Convert" else "Add More Photos",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${selectedImages.size} photos selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }

                if (selectedImages.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Page Order Preview",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textSecondary,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(selectedImages) { index, uri ->
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.LightGray)
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Page badge
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = "${index + 1}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                // Delete button
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(20.dp)
                                        .clip(PillShape)
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .clickable { selectedImages.removeAt(index) },
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
                        label = { Text("PDF Output Filename") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (isProcessing) {
                        CircularProgressIndicator(color = ElectricBlue)
                    } else {
                        GradientButton(
                            text = "Generate PDF (${selectedImages.size} Pages)",
                            onClick = {
                                scope.launch {
                                    isProcessing = true
                                    try {
                                        val validName = if (pdfTitle.endsWith(".pdf")) pdfTitle else "$pdfTitle.pdf"
                                        result = ImageToPdfEngine.createPdfFromImages(
                                            context = context,
                                            imageUris = selectedImages.toList(),
                                            outputFileName = validName
                                        )
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Failed to create PDF: ${e.localizedMessage}")
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
                        text = "PDF Created Successfully!",
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
                        text = "Download PDF",
                        onClick = {
                            FileHelper.saveToDownloads(context, res.outputFile, res.fileName, res.mimeType)
                            HistoryManager.getInstance(context).addItem(
                                HistoryItem(
                                    fileName = res.fileName,
                                    originalSizeBytes = res.originalSizeBytes,
                                    outputSizeBytes = res.compressedSizeBytes,
                                    operationType = "Image to PDF",
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
            currentRoute = Screen.ImageToPdf.route,
            onNavigateToRoute = onNavigateToRoute
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

