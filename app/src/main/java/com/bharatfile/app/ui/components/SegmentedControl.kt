package com.bharatfile.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bharatfile.app.model.DocumentType
import com.bharatfile.app.theme.BharatFileThemeCustom
import com.bharatfile.app.theme.PillShape

@Composable
fun SegmentedControl(
    selectedType: DocumentType,
    onTypeSelected: (DocumentType) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = BharatFileThemeCustom.colors
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(46.dp)
            .background(color = colors.segmentedContainer, shape = PillShape)
            .border(width = 1.dp, color = colors.cardBorder.copy(alpha = 0.6f), shape = PillShape)
            .padding(3.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val segmentWidth = maxWidth / 2
            val animatedOffset by animateDpAsState(
                targetValue = if (selectedType == DocumentType.PDF) 0.dp else segmentWidth,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "indicatorOffset"
            )

            // Animated Gradient Pill Indicator
            Box(
                modifier = Modifier
                    .offset(x = animatedOffset)
                    .width(segmentWidth)
                    .fillMaxHeight()
                    .shadow(elevation = 6.dp, shape = PillShape, spotColor = colors.glowColor)
                    .background(brush = colors.brandGradient, shape = PillShape)
            )

            // Clickable Options Row
            Row(modifier = Modifier.fillMaxSize()) {
                // PDF Tab
                SegmentTab(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    title = "PDF",
                    icon = Icons.Outlined.Description,
                    isSelected = selectedType == DocumentType.PDF,
                    onClick = { onTypeSelected(DocumentType.PDF) },
                    interactionSource = interactionSource
                )

                // Image Tab
                SegmentTab(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    title = "Image",
                    icon = Icons.Outlined.Image,
                    isSelected = selectedType == DocumentType.IMAGE,
                    onClick = { onTypeSelected(DocumentType.IMAGE) },
                    interactionSource = interactionSource
                )
            }
        }
    }
}

@Composable
private fun SegmentTab(
    modifier: Modifier,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource
) {
    val colors = BharatFileThemeCustom.colors
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else colors.textSecondary,
        animationSpec = tween(durationMillis = 200),
        label = "tabContentColor"
    )

    Box(
        modifier = modifier
            .clip(PillShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = contentColor
            )
        }
    }
}
