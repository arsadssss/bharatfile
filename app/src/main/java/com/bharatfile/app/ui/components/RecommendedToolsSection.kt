package com.bharatfile.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CallSplit
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Transform
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.material3.Icon
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
import com.bharatfile.app.navigation.Screen
import com.bharatfile.app.theme.BharatFileThemeCustom
import com.bharatfile.app.theme.ElectricBlue

data class RecommendedTool(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String
)

object ToolRecommendations {
    fun getRecommendations(currentRoute: String): List<RecommendedTool> {
        return when (currentRoute) {
            Screen.Home.route, "compress_pdf" -> listOf(
                RecommendedTool("Merge PDF", "Combine multiple documents into one", Icons.Outlined.Layers, Screen.PdfMerge.route),
                RecommendedTool("PDF to Image", "Extract pages as clear JPG or PNG", Icons.Outlined.Image, Screen.PdfToImage.route),
                RecommendedTool("Split PDF", "Separate marks sheets or specific pages", Icons.Outlined.CallSplit, Screen.PdfSplit.route)
            )
            "compress_image" -> listOf(
                RecommendedTool("Resize Image", "Set precise width, height, or aspect ratio", Icons.Outlined.Crop, Screen.ResizeImage.route),
                RecommendedTool("Convert Format", "Convert between JPG, PNG, and WebP", Icons.Outlined.Transform, Screen.ConvertImage.route),
                RecommendedTool("Image to PDF", "Generate clean PDF from photo captures", Icons.Outlined.PictureAsPdf, Screen.ImageToPdf.route)
            )
            Screen.ResizeImage.route -> listOf(
                RecommendedTool("Compress Image", "Reduce MB to KB for portal limits", Icons.Outlined.FolderZip, "home_image"),
                RecommendedTool("Passport Photo", "Make 3.5×4.5 cm photos with blue/white bg", Icons.Outlined.Badge, Screen.PassportPhoto.route),
                RecommendedTool("Convert Format", "Switch between JPG, PNG, and WebP", Icons.Outlined.Transform, Screen.ConvertImage.route)
            )
            Screen.ConvertImage.route -> listOf(
                RecommendedTool("Compress Image", "Keep quality high while shrinking file size", Icons.Outlined.FolderZip, "home_image"),
                RecommendedTool("Resize Image", "Scale dimensions for online forms", Icons.Outlined.Crop, Screen.ResizeImage.route),
                RecommendedTool("Image to PDF", "Save converted images as a neat PDF", Icons.Outlined.PictureAsPdf, Screen.ImageToPdf.route)
            )
            Screen.ImageToPdf.route -> listOf(
                RecommendedTool("Compress PDF", "Shrink generated PDF under 200 KB", Icons.Outlined.FolderZip, Screen.Home.route),
                RecommendedTool("Merge PDF", "Combine with other application papers", Icons.Outlined.Layers, Screen.PdfMerge.route),
                RecommendedTool("Doc Scanner", "Enhance lighting & crop documents first", Icons.Outlined.CameraAlt, Screen.Scanner.route)
            )
            Screen.PdfToImage.route -> listOf(
                RecommendedTool("Compress Image", "Reduce size of extracted page images", Icons.Outlined.FolderZip, "home_image"),
                RecommendedTool("Resize Image", "Scale page dimensions", Icons.Outlined.Crop, Screen.ResizeImage.route),
                RecommendedTool("Convert Image", "Switch formats between JPG & PNG", Icons.Outlined.Transform, Screen.ConvertImage.route)
            )
            Screen.PdfMerge.route -> listOf(
                RecommendedTool("Compress PDF", "Reduce combined PDF to portal limits", Icons.Outlined.FolderZip, Screen.Home.route),
                RecommendedTool("Split PDF", "Extract specific sections or certificates", Icons.Outlined.CallSplit, Screen.PdfSplit.route),
                RecommendedTool("Organize Pages", "Reorder, rotate, or remove pages", Icons.Outlined.ViewWeek, Screen.PdfOrganize.route)
            )
            Screen.PdfSplit.route -> listOf(
                RecommendedTool("Merge PDF", "Reassemble selected pages into a new file", Icons.Outlined.Layers, Screen.PdfMerge.route),
                RecommendedTool("Compress PDF", "Optimize the split PDF file size", Icons.Outlined.FolderZip, Screen.Home.route),
                RecommendedTool("Organize Pages", "Rotate or delete individual pages", Icons.Outlined.ViewWeek, Screen.PdfOrganize.route)
            )
            Screen.PdfOrganize.route -> listOf(
                RecommendedTool("Compress PDF", "Shrink reorganized PDF for upload", Icons.Outlined.FolderZip, Screen.Home.route),
                RecommendedTool("Merge PDF", "Append additional certificates", Icons.Outlined.Layers, Screen.PdfMerge.route),
                RecommendedTool("Split PDF", "Save separate page ranges", Icons.Outlined.CallSplit, Screen.PdfSplit.route)
            )
            Screen.Scanner.route -> listOf(
                RecommendedTool("Compress PDF", "Reduce scan file size for job portals", Icons.Outlined.FolderZip, Screen.Home.route),
                RecommendedTool("Signature Maker", "Create transparent signature PNG", Icons.Outlined.Draw, Screen.SignatureMaker.route),
                RecommendedTool("Image to PDF", "Compile scanned pages into multi-page PDF", Icons.Outlined.PictureAsPdf, Screen.ImageToPdf.route)
            )
            Screen.SignatureMaker.route -> listOf(
                RecommendedTool("Passport Photo", "Prepare candidate photo for online forms", Icons.Outlined.Badge, Screen.PassportPhoto.route),
                RecommendedTool("Resize Image", "Exact pixel dimensions (140×60 px)", Icons.Outlined.Crop, Screen.ResizeImage.route),
                RecommendedTool("Doc Scanner", "Scan physical signed papers", Icons.Outlined.CameraAlt, Screen.Scanner.route)
            )
            Screen.PassportPhoto.route -> listOf(
                RecommendedTool("Signature Maker", "Prepare matching digital signature", Icons.Outlined.Draw, Screen.SignatureMaker.route),
                RecommendedTool("Compress Image", "Ensure photo is under 50 KB requirement", Icons.Outlined.FolderZip, "home_image"),
                RecommendedTool("Resize Image", "Adjust exact pixel dimensions", Icons.Outlined.Crop, Screen.ResizeImage.route)
            )
            else -> listOf(
                RecommendedTool("Compress PDF", "Reduce PDF size with quality intact", Icons.Outlined.FolderZip, Screen.Home.route),
                RecommendedTool("Compress Image", "Optimize JPG/PNG files under 100 KB", Icons.Outlined.Image, "home_image"),
                RecommendedTool("Doc Scanner", "Instant camera scan with filters", Icons.Outlined.CameraAlt, Screen.Scanner.route)
            )
        }
    }
}

@Composable
fun RecommendedToolsSection(
    currentRoute: String,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = BharatFileThemeCustom.colors
    val recommendations = ToolRecommendations.getRecommendations(currentRoute)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(ElectricBlue)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Recommended Next Tools",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = colors.textPrimary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            recommendations.forEach { tool ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToRoute(tool.route) },
                    elevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ElectricBlue.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tool.icon,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tool.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                ),
                                color = colors.textPrimary
                            )
                            Text(
                                text = tool.description,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = colors.textSecondary,
                                maxLines = 1
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = "Navigate",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
