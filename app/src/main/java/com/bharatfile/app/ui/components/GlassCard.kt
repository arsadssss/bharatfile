package com.bharatfile.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bharatfile.app.theme.BharatFileThemeCustom
import com.bharatfile.app.theme.CardShape

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = CardShape,
    elevation: Dp = 4.dp,
    borderWidth: Dp = 1.dp,
    backgroundColor: Color = BharatFileThemeCustom.colors.cardSurface,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = BharatFileThemeCustom.colors

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color(0xFF4E65FF).copy(alpha = 0.08f)
            )
            .clip(shape)
            .background(color = backgroundColor)
            .border(
                width = borderWidth,
                color = colors.cardBorder,
                shape = shape
            )
    ) {
        content()
    }
}
