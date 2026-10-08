package com.bharatfile.app.ui.components.illustrations

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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

@Composable
fun ProcessingIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 150.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "processingDocAnim")

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "docFloat"
    )

    val sphere1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sphere1"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Soft circular glowing backdrop matching Screen 2
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFE0EAFF).copy(alpha = 0.85f),
                        Color(0xFFEEF2FF).copy(alpha = 0.50f),
                        Color(0xFFF6F8FE).copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.52f),
                    radius = w * 0.48f
                )
            )

            // Floating 3D Spheres / Pearls around document
            // Left sphere
            val rad1 = Math.toRadians((sphere1Offset).toDouble())
            val orb1X = (w * 0.22f + Math.sin(rad1) * 6).toFloat()
            val orb1Y = (h * 0.52f + Math.cos(rad1) * 6).toFloat()
            drawSphere(orb1X, orb1Y, w * 0.038f, Color(0xFF4E65FF))

            // Right top sphere
            val rad2 = Math.toRadians((sphere1Offset + 120).toDouble())
            val orb2X = (w * 0.78f + Math.sin(rad2) * 8).toFloat()
            val orb2Y = (h * 0.38f + Math.cos(rad2) * 8).toFloat()
            drawSphere(orb2X, orb2Y, w * 0.026f, Color(0xFF7A5AF8))

            // Right lower sphere
            val rad3 = Math.toRadians((sphere1Offset + 240).toDouble())
            val orb3X = (w * 0.74f + Math.cos(rad3) * 6).toFloat()
            val orb3Y = (h * 0.68f + Math.sin(rad3) * 6).toFloat()
            drawSphere(orb3X, orb3Y, w * 0.042f, Color(0xFF6366F1))

            // Floating Stack of 3 Papers
            val sheetW = w * 0.44f
            val sheetH = h * 0.52f

            // Bottom paper sheet
            rotate(degrees = -10f, pivot = Offset(w * 0.5f, h * 0.52f + floatOffset)) {
                drawRoundRect(
                    color = Color(0xFFC7D2FE).copy(alpha = 0.75f),
                    topLeft = Offset(w * 0.28f, h * 0.25f + floatOffset),
                    size = Size(sheetW, sheetH),
                    cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
                )
            }

            // Middle paper sheet
            rotate(degrees = -4f, pivot = Offset(w * 0.5f, h * 0.52f + floatOffset)) {
                drawRoundRect(
                    color = Color(0xFFE0E7FF),
                    topLeft = Offset(w * 0.28f, h * 0.25f + floatOffset),
                    size = Size(sheetW, sheetH),
                    cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
                )
            }

            // Top paper sheet (Soft white with crisp glass edge)
            rotate(degrees = 3f, pivot = Offset(w * 0.5f, h * 0.52f + floatOffset)) {
                // Drop shadow
                drawRoundRect(
                    color = Color(0xFF1E293B).copy(alpha = 0.10f),
                    topLeft = Offset(w * 0.28f, h * 0.25f + floatOffset + 4f),
                    size = Size(sheetW, sheetH),
                    cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
                )
                // White sheet
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFF8FAFC)
                        )
                    ),
                    topLeft = Offset(w * 0.28f, h * 0.25f + floatOffset),
                    size = Size(sheetW, sheetH),
                    cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
                )
                // Translucent border
                drawRoundRect(
                    color = Color(0xFFE2E8F0),
                    topLeft = Offset(w * 0.28f, h * 0.25f + floatOffset),
                    size = Size(sheetW, sheetH),
                    cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                )

                // 3D Red PDF Badge on top
                val badgeSize = w * 0.20f
                val badgeX = w * 0.50f - badgeSize / 2f
                val badgeY = h * 0.51f - badgeSize / 2f + floatOffset

                // Badge shadow
                drawRoundRect(
                    color = Color(0xFFB42318).copy(alpha = 0.35f),
                    topLeft = Offset(badgeX, badgeY + 3f),
                    size = Size(badgeSize, badgeSize),
                    cornerRadius = CornerRadius(badgeSize * 0.30f, badgeSize * 0.30f)
                )
                // Red badge gradient
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF97066),
                            Color(0xFFF04438),
                            Color(0xFFD92D20)
                        )
                    ),
                    topLeft = Offset(badgeX, badgeY),
                    size = Size(badgeSize, badgeSize),
                    cornerRadius = CornerRadius(badgeSize * 0.30f, badgeSize * 0.30f)
                )

                // Stylized PDF document icon in badge
                val path = Path().apply {
                    moveTo(badgeX + badgeSize * 0.25f, badgeY + badgeSize * 0.20f)
                    lineTo(badgeX + badgeSize * 0.55f, badgeY + badgeSize * 0.20f)
                    lineTo(badgeX + badgeSize * 0.75f, badgeY + badgeSize * 0.40f)
                    lineTo(badgeX + badgeSize * 0.75f, badgeY + badgeSize * 0.80f)
                    lineTo(badgeX + badgeSize * 0.25f, badgeY + badgeSize * 0.80f)
                    close()
                }
                drawPath(path, Color.White.copy(alpha = 0.95f))
            }
        }
    }
}

private fun DrawScope.drawSphere(cx: Float, cy: Float, radius: Float, baseColor: Color) {
    // 3D sphere gradient with light highlight on top-left
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.8f),
                baseColor,
                baseColor.copy(alpha = 0.9f)
            ),
            center = Offset(cx - radius * 0.35f, cy - radius * 0.35f),
            radius = radius * 1.2f
        ),
        radius = radius,
        center = Offset(cx, cy)
    )
}
