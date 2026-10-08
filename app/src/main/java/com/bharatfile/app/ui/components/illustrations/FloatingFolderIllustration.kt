package com.bharatfile.app.ui.components.illustrations

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bharatfile.app.model.DocumentType

@Composable
fun FloatingFolderIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    documentType: DocumentType = DocumentType.PDF
) {
    val infiniteTransition = rememberInfiniteTransition(label = "folderFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "folderFloatOffset"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(size)
                .offset(y = floatOffset.dp)
        ) {
            val w = this.size.width
            val h = this.size.height

            // Soft atmospheric ambient glow behind the folder
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF6366F1).copy(alpha = 0.22f),
                        Color(0xFF8B5CF6).copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.55f),
                    radius = w * 0.55f
                )
            )

            // Back Folder Layer (Translucent purple/indigo folder)
            rotate(degrees = -8f, pivot = Offset(w * 0.45f, h * 0.5f)) {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6B7280).copy(alpha = 0.25f),
                            Color(0xFF4F46E5).copy(alpha = 0.65f),
                            Color(0xFF4338CA).copy(alpha = 0.85f)
                        )
                    ),
                    topLeft = Offset(w * 0.22f, h * 0.26f),
                    size = Size(w * 0.52f, h * 0.56f),
                    cornerRadius = CornerRadius(w * 0.10f, w * 0.10f)
                )
            }

            // Middle Translucent Sheet
            rotate(degrees = -2f, pivot = Offset(w * 0.5f, h * 0.5f)) {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF).copy(alpha = 0.70f),
                            Color(0xFFE0E7FF).copy(alpha = 0.50f)
                        )
                    ),
                    topLeft = Offset(w * 0.28f, h * 0.22f),
                    size = Size(w * 0.52f, h * 0.58f),
                    cornerRadius = CornerRadius(w * 0.10f, w * 0.10f)
                )
            }

            // Front Main Translucent Sheet (Frosted Glass)
            rotate(degrees = 5f, pivot = Offset(w * 0.52f, h * 0.55f)) {
                // Card shadow
                drawRoundRect(
                    color = Color(0xFF1E1B4B).copy(alpha = 0.12f),
                    topLeft = Offset(w * 0.35f, h * 0.24f + 4f),
                    size = Size(w * 0.52f, h * 0.58f),
                    cornerRadius = CornerRadius(w * 0.10f, w * 0.10f)
                )
                // Card body
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF).copy(alpha = 0.95f),
                            Color(0xFFF5F3FF).copy(alpha = 0.85f),
                            Color(0xFFDDD6FE).copy(alpha = 0.75f)
                        )
                    ),
                    topLeft = Offset(w * 0.35f, h * 0.22f),
                    size = Size(w * 0.52f, h * 0.58f),
                    cornerRadius = CornerRadius(w * 0.10f, w * 0.10f)
                )

                // Subtle inner border (glassmorphism edge)
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.9f),
                            Color(0xFFC7D2FE).copy(alpha = 0.4f)
                        )
                    ),
                    topLeft = Offset(w * 0.35f, h * 0.22f),
                    size = Size(w * 0.52f, h * 0.58f),
                    cornerRadius = CornerRadius(w * 0.10f, w * 0.10f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.8f)
                )

                // Central Badge
                val badgeCenterX = w * 0.61f
                val badgeCenterY = h * 0.51f
                val badgeSize = w * 0.26f

                if (documentType == DocumentType.PDF) {
                    // Crimson 3D PDF Badge
                    drawPdfBadge(badgeCenterX, badgeCenterY, badgeSize)
                } else {
                    // Sky Blue Image Badge
                    drawImageBadge(badgeCenterX, badgeCenterY, badgeSize)
                }
            }
        }
    }
}

private fun DrawScope.drawPdfBadge(centerX: Float, centerY: Float, size: Float) {
    val half = size / 2f
    // Badge shadow
    drawRoundRect(
        color = Color(0xFFB42318).copy(alpha = 0.35f),
        topLeft = Offset(centerX - half, centerY - half + 3f),
        size = Size(size, size),
        cornerRadius = CornerRadius(size * 0.28f, size * 0.28f)
    )
    // Badge gradient
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF97066),
                Color(0xFFF04438),
                Color(0xFFD92D20)
            )
        ),
        topLeft = Offset(centerX - half, centerY - half),
        size = Size(size, size),
        cornerRadius = CornerRadius(size * 0.28f, size * 0.28f)
    )

    // Stylized PDF document icon shape inside badge
    val path = Path().apply {
        moveTo(centerX - size * 0.20f, centerY - size * 0.18f)
        lineTo(centerX + size * 0.08f, centerY - size * 0.18f)
        lineTo(centerX + size * 0.20f, centerY - size * 0.06f)
        lineTo(centerX + size * 0.20f, centerY + size * 0.22f)
        lineTo(centerX - size * 0.20f, centerY + size * 0.22f)
        close()
    }
    drawPath(path, Color.White.copy(alpha = 0.95f))

    // Folded top-right corner
    val fold = Path().apply {
        moveTo(centerX + size * 0.08f, centerY - size * 0.18f)
        lineTo(centerX + size * 0.08f, centerY - size * 0.06f)
        lineTo(centerX + size * 0.20f, centerY - size * 0.06f)
        close()
    }
    drawPath(fold, Color(0xFFFEE4E2))
}

private fun DrawScope.drawImageBadge(centerX: Float, centerY: Float, size: Float) {
    val half = size / 2f
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF60A5FA),
                Color(0xFF3B82F6),
                Color(0xFF2563EB)
            )
        ),
        topLeft = Offset(centerX - half, centerY - half),
        size = Size(size, size),
        cornerRadius = CornerRadius(size * 0.28f, size * 0.28f)
    )
    // Mountain / Sun landscape icon inside
    drawCircle(
        color = Color(0xFFFEF08A),
        radius = size * 0.08f,
        center = Offset(centerX + size * 0.12f, centerY - size * 0.12f)
    )
    val mountain = Path().apply {
        moveTo(centerX - size * 0.22f, centerY + size * 0.18f)
        lineTo(centerX - size * 0.04f, centerY)
        lineTo(centerX + size * 0.06f, centerY + size * 0.08f)
        lineTo(centerX + size * 0.16f, centerY - size * 0.05f)
        lineTo(centerX + size * 0.24f, centerY + size * 0.18f)
        close()
    }
    drawPath(mountain, Color.White)
}
