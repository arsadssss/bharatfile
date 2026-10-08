package com.bharatfile.app.ui.screens

import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.CallSplit
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.bharatfile.app.engine.PdfSplitter
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
fun PdfSplitScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRoute: (String) -> Unit = {},
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = BharatFileThemeCustom.colors
    val scrollState = rememberScrollState()

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedName by remember { mutableStateOf("") }
    var totalPages by remember { mutableIntStateOf(0) }
    var pageRangeText by remember { mutableStateOf("1-2") }
    var isProcessing by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<ProcessedFileResult?>(null) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            selectedUri = it
            selectedName = FileHelper.getFileName(context, it)
            result = null
            scope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        val temp = FileHelper.copyUriToCacheFile(context, it, "split_count_")
                        val pfd = ParcelFileDescriptor.open(temp, ParcelFileDescriptor.MODE_READ_ONLY)
                        val r = PdfRenderer(pfd)
                        totalPages = r.pageCount
                        r.close()
                        pfd.close()
                        temp.delete()
                    } catch (_: Exception) {}
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
                text = "Split PDF",
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
                if (selectedUri == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(InnerCardShape)
                            .background(colors.segmentedContainer)
                            .clickable { pickerLauncher.launch(arrayOf("application/pdf")) }
                            .padding(vertical = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.CallSplit,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Select PDF Document",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(InnerCardShape)
                            .background(colors.segmentedContainer)
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary,
                                maxLines = 1
                            )
                            Text(
                                text = "Total Pages: $totalPages",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(ElectricBlue.copy(alpha = 0.15f))
                                .clickable { pickerLauncher.launch(arrayOf("application/pdf")) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = "Change", color = ElectricBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = pageRangeText,
                        onValueChange = { pageRangeText = it },
                        label = { Text("Specify Pages to Extract") },
                        placeholder = { Text("e.g. 1-3, 5, 8-10") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Separate ranges with commas (e.g. 1-2, 4). Total pages available: $totalPages",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (isProcessing) {
                        CircularProgressIndicator(color = ElectricBlue)
                    } else {
                        GradientButton(
                            text = "Split & Extract Pages",
                            onClick = {
                                val uri = selectedUri ?: return@GradientButton
                                val pages = PdfSplitter.parsePageRanges(pageRangeText, totalPages)
                                if (pages.isEmpty()) {
                                    scope.launch { snackbarHostState.showSnackbar("Please enter valid page numbers (1 to $totalPages)") }
                                    return@GradientButton
                                }

                                scope.launch {
                                    isProcessing = true
                                    try {
                                        result = PdfSplitter.splitPdf(
                                            context = context,
                                            inputUri = uri,
                                            pageNumbers = pages,
                                            outputName = "${selectedName.substringBeforeLast('.')}_split.pdf"
                                        )
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Split error: ${e.localizedMessage}")
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
                        text = "PDF Split Successfully!",
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
                        text = "Download Split PDF",
                        onClick = {
                            FileHelper.saveToDownloads(context, res.outputFile, res.fileName, res.mimeType)
                            HistoryManager.getInstance(context).addItem(
                                HistoryItem(
                                    fileName = res.fileName,
                                    originalSizeBytes = res.originalSizeBytes,
                                    outputSizeBytes = res.compressedSizeBytes,
                                    operationType = "PDF Split",
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
            currentRoute = Screen.PdfSplit.route,
            onNavigateToRoute = onNavigateToRoute
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

