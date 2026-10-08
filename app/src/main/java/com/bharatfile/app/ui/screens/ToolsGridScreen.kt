package com.bharatfile.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.FlipToBack
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Merge
import androidx.compose.material.icons.outlined.PhotoCameraFront
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bharatfile.app.model.ToolCategory
import com.bharatfile.app.model.ToolItem
import com.bharatfile.app.theme.BharatFileThemeCustom
import com.bharatfile.app.theme.ElectricBlue
import com.bharatfile.app.theme.ToolCardShape
import com.bharatfile.app.ui.components.GlassCard

data class ToolDefinition(
    val id: String,
    val title: String,
    val description: String,
    val route: String,
    val icon: ImageVector,
    val bgLight: Color,
    val accentColor: Color,
    val category: String,
    val badge: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsGridScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = BharatFileThemeCustom.colors

    val allTools = listOf(
        // Core & Compress
        ToolDefinition("compress_pdf", "Compress PDF", "Reduce PDF file size", "home_pdf", Icons.Outlined.Description, colors.pastelBlueBg, colors.pastelBlueText, "Core Tools", "Popular"),
        ToolDefinition("compress_image", "Compress Image", "Shrink JPG/PNG/WebP", "home_image", Icons.Outlined.Image, colors.pastelOrangeBg, colors.pastelOrangeText, "Core Tools"),
        ToolDefinition("resize_image", "Resize Image", "Custom dimensions & KB", "resize_image", Icons.Outlined.AspectRatio, colors.pastelPurpleBg, colors.pastelPurpleText, "Core Tools"),
        ToolDefinition("convert_image", "Convert Image", "JPG ↔ PNG ↔ WebP", "convert_image", Icons.Outlined.SwapHoriz, colors.pastelGreenBg, colors.pastelGreenText, "Core Tools"),

        // PDF Utilities
        ToolDefinition("image_to_pdf", "Image to PDF", "Combine photos to PDF", "image_to_pdf", Icons.Outlined.FlipToBack, colors.pastelBlueBg, colors.pastelBlueText, "PDF Utilities"),
        ToolDefinition("pdf_to_image", "PDF to Image", "Extract pages as images", "pdf_to_image", Icons.Outlined.Image, colors.pastelOrangeBg, colors.pastelOrangeText, "PDF Utilities"),
        ToolDefinition("merge_pdf", "Merge PDF", "Combine multiple PDFs", "merge_pdf", Icons.Outlined.Merge, colors.pastelPurpleBg, colors.pastelPurpleText, "PDF Utilities"),
        ToolDefinition("split_pdf", "Split PDF", "Extract pages or ranges", "split_pdf", Icons.Outlined.Crop, colors.pastelGreenBg, colors.pastelGreenText, "PDF Utilities"),
        ToolDefinition("organize_pdf", "Organize PDF", "Reorder, rotate & delete", "organize_pdf", Icons.Outlined.GridOn, colors.pastelOrangeBg, colors.pastelOrangeText, "PDF Utilities"),

        // Student & Utility
        ToolDefinition("scan_doc", "Scan Document", "Camera scanner to PDF", "scanner", Icons.Outlined.CameraAlt, colors.pastelBlueBg, colors.pastelBlueText, "Student & Office", "New"),
        ToolDefinition("signature_maker", "Signature Maker", "Transparent sign PNG", "signature_maker", Icons.Outlined.Draw, colors.pastelPurpleBg, colors.pastelPurpleText, "Student & Office"),
        ToolDefinition("passport_photo", "Passport Photo Crop", "UPSC, SSC & Exam crop", "passport_photo", Icons.Outlined.PhotoCameraFront, colors.pastelGreenBg, colors.pastelGreenText, "Student & Office", "Exams")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        // App bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "All Tools",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = colors.textPrimary
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Group by category
            val grouped = allTools.groupBy { it.category }

            grouped.forEach { (categoryName, toolsInCategory) ->
                item(span = { GridItemSpan(2) }) {
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = colors.textSecondary,
                        modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                    )
                }

                items(toolsInCategory) { tool ->
                    ToolCardItem(
                        tool = tool,
                        onClick = { onNavigateToRoute(tool.route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolCardItem(
    tool: ToolDefinition,
    onClick: () -> Unit
) {
    val colors = BharatFileThemeCustom.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ToolCardShape)
            .background(tool.bgLight)
            .border(width = 1.dp, color = tool.accentColor.copy(alpha = 0.25f), shape = ToolCardShape)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Icon squircle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.90f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tool.icon,
                        contentDescription = tool.title,
                        tint = tool.accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                if (tool.badge != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(tool.accentColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tool.badge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = tool.accentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = tool.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = colors.textPrimary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = tool.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp
                ),
                color = colors.textSecondary,
                maxLines = 1
            )
        }
    }
}
