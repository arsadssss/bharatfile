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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SuccessIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 150.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "successDocAnim")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "successFloat"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Soft glowing emerald green halo disc (matching Screen 3)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFD1FADF).copy(alpha = 0.85f),
                        Color(0xFFECFDF3).copy(alpha = 0.50f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.52f),
                    radius = w * 0.46f
                )
            )

            // Tiny celebration confetti dots around the halo
            drawCircle(color = Color(0xFFFDB022), radius = w * 0.016f, center = Offset(w * 0.32f, h * 0.35f))
            drawCircle(color = Color(0xFF12B76A), radius = w * 0.022f, center = Offset(w * 0.72f, h * 0.33f))
            drawCircle(color = Color(0xFF06AED4), radius = w * 0.018f, center = Offset(w * 0.28f, h * 0.68f))
            drawCircle(color = Color(0xFFF97066), radius = w * 0.018f, center = Offset(w * 0.75f, h * 0.65f))

            // Floating Translucent White Document
            val docW = w * 0.44f
            val docH = h * 0.54f
            val docX = (w - docW) / 2f
            val docY = (h - docH) / 2f + floatOffset

            // Document drop shadow
            drawRoundRect(
                color = Color(0xFF039855).copy(alpha = 0.12f),
                topLeft = Offset(docX, docY + 6f),
                size = Size(docW, docH),
                cornerRadius = CornerRadius(w * 0.09f, w * 0.09f)
            )

            // Translucent document body
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFF6FEF9)
                    )
                ),
                topLeft = Offset(docX, docY),
                size = Size(docW, docH),
                cornerRadius = CornerRadius(w * 0.09f, w * 0.09f)
            )

            // Crisp soft outline
            drawRoundRect(
                color = Color(0xFFA6F4C5).copy(alpha = 0.8f),
                topLeft = Offset(docX, docY),
                size = Size(docW, docH),
                cornerRadius = CornerRadius(w * 0.09f, w * 0.09f),
                style = Stroke(width = 1.6f)
            )

            // Folded corner on top-right of document
            val foldSize = w * 0.12f
            val foldPath = Path().apply {
                moveTo(docX + docW - foldSize, docY)
                lineTo(docX + docW, docY + foldSize)
                lineTo(docX + docW - foldSize, docY + foldSize)
                close()
            }
            drawPath(foldPath, Color(0xFFD1FADF))

            // Central Emerald Green Checkmark Circle
            val checkRadius = w * 0.13f
            val checkCenterX = w * 0.5f
            val checkCenterY = docY + docH * 0.56f

            // Shadow under check circle
            drawCircle(
                color = Color(0xFF039855).copy(alpha = 0.35f),
                radius = checkRadius,
                center = Offset(checkCenterX, checkCenterY + 3f)
            )

            // Emerald Green gradient check circle
            drawCircle(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF32D583),
                        Color(0xFF12B76A),
                        Color(0xFF039855)
                    )
                ),
                radius = checkRadius,
                center = Offset(checkCenterX, checkCenterY)
            )

            // White Checkmark mark inside
            val checkmark = Path().apply {
                moveTo(checkCenterX - checkRadius * 0.42f, checkCenterY + checkRadius * 0.02f)
                lineTo(checkCenterX - checkRadius * 0.08f, checkCenterY + checkRadius * 0.36f)
                lineTo(checkCenterX + checkRadius * 0.42f, checkCenterY - checkRadius * 0.32f)
            }
            drawPath(
                path = checkmark,
                color = Color.White,
                style = Stroke(width = 4.2f, cap = StrokeCap.Round)
            )
        }
    }
}
