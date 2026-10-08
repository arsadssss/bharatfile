package com.bharatfile.app.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint as AndroidPaint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bharatfile.app.data.HistoryManager
import com.bharatfile.app.engine.FileHelper
import com.bharatfile.app.engine.SignatureEngine
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

data class SignaturePath(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

@Composable
fun SignatureMakerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRoute: (String) -> Unit = {},
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = BharatFileThemeCustom.colors
    val scrollState = rememberScrollState()

    val paths = remember { mutableStateListOf<SignaturePath>() }
    var currentPoints by remember { mutableStateOf(listOf<Offset>()) }
    var selectedColor by remember { mutableStateOf(Color.Black) }
    var strokeWidth by remember { mutableStateOf(6f) }
    var result by remember { mutableStateOf<ProcessedFileResult?>(null) }

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
                text = "Signature Maker",
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
                Text(
                    text = "Sign below using your finger / stylus",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Drawing Pad
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(InnerCardShape)
                        .background(Color.White)
                        .border(1.2.dp, colors.dashedBorder, InnerCardShape)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(selectedColor, strokeWidth) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        currentPoints = listOf(offset)
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        currentPoints = currentPoints + change.position
                                    },
                                    onDragEnd = {
                                        if (currentPoints.isNotEmpty()) {
                                            paths.add(SignaturePath(currentPoints, selectedColor, strokeWidth))
                                            currentPoints = emptyList()
                                        }
                                    }
                                )
                            }
                    ) {
                        paths.forEach { sigPath ->
                            if (sigPath.points.size > 1) {
                                val p = Path().apply {
                                    moveTo(sigPath.points.first().x, sigPath.points.first().y)
                                    for (pt in sigPath.points.drop(1)) {
                                        lineTo(pt.x, pt.y)
                                    }
                                }
                                drawPath(
                                    path = p,
                                    color = sigPath.color,
                                    style = Stroke(
                                        width = sigPath.strokeWidth,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }
                        }

                        if (currentPoints.size > 1) {
                            val p = Path().apply {
                                moveTo(currentPoints.first().x, currentPoints.first().y)
                                for (pt in currentPoints.drop(1)) {
                                    lineTo(pt.x, pt.y)
                                }
                            }
                            drawPath(
                                path = p,
                                color = selectedColor,
                                style = Stroke(
                                    width = strokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }

                    // Watermark guide line
                    Text(
                        text = "Sign Here ✍️",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Ink Color & Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(Color.Black, Color(0xFF1E40AF), Color(0xFF991B1B)).forEach { col ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(col)
                                    .clickable { selectedColor = col }
                                    .border(
                                        width = if (selectedColor == col) 2.dp else 0.dp,
                                        color = if (selectedColor == col) ElectricBlue else Color.Transparent,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(colors.cardBorder)
                            .clickable {
                                paths.clear()
                                currentPoints = emptyList()
                                result = null
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Clear", fontSize = 12.sp, color = colors.textPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                GradientButton(
                    text = "Save Transparent Signature (PNG)",
                    enabled = paths.isNotEmpty(),
                    onClick = {
                        scope.launch {
                            try {
                                val bitmap = Bitmap.createBitmap(800, 520, Bitmap.Config.ARGB_8888)
                                val canvas = Canvas(bitmap)

                                val paint = AndroidPaint().apply {
                                    isAntiAlias = true
                                    style = AndroidPaint.Style.STROKE
                                    strokeCap = AndroidPaint.Cap.ROUND
                                    strokeJoin = AndroidPaint.Join.ROUND
                                }

                                paths.forEach { sp ->
                                    paint.strokeWidth = sp.strokeWidth * 1.5f
                                    paint.color = AndroidColor.argb(
                                        (sp.color.alpha * 255).toInt(),
                                        (sp.color.red * 255).toInt(),
                                        (sp.color.green * 255).toInt(),
                                        (sp.color.blue * 255).toInt()
                                    )
                                    val androidPath = android.graphics.Path()
                                    if (sp.points.isNotEmpty()) {
                                        androidPath.moveTo(sp.points.first().x * 2f, sp.points.first().y * 2f)
                                        for (pt in sp.points.drop(1)) {
                                            androidPath.lineTo(pt.x * 2f, pt.y * 2f)
                                        }
                                    }
                                    canvas.drawPath(androidPath, paint)
                                }

                                result = SignatureEngine.saveSignature(context, bitmap, transparentBg = true)
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Failed to export: ${e.localizedMessage}")
                            }
                        }
                    }
                )
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
                        text = "Signature PNG Ready!",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = SuccessGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Transparent background, ready for online application forms",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    GradientButton(
                        text = "Download Signature",
                        onClick = {
                            FileHelper.saveToDownloads(context, res.outputFile, res.fileName, res.mimeType)
                            HistoryManager.getInstance(context).addItem(
                                HistoryItem(
                                    fileName = res.fileName,
                                    originalSizeBytes = res.originalSizeBytes,
                                    outputSizeBytes = res.compressedSizeBytes,
                                    operationType = "Signature Maker",
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
            currentRoute = Screen.SignatureMaker.route,
            onNavigateToRoute = onNavigateToRoute
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

