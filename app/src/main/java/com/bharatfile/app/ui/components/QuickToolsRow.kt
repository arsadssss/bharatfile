package com.bharatfile.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bharatfile.app.theme.BharatFileThemeCustom
import com.bharatfile.app.theme.ToolCardShape

@Composable
fun QuickToolsRow(
    onNavigateToImages: () -> Unit,
    onNavigateToResize: () -> Unit,
    onNavigateToConvert: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = BharatFileThemeCustom.colors

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "You may also like",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            ),
            color = colors.textPrimary,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PastelToolCard(
                title = "Images",
                icon = Icons.Outlined.Image,
                backgroundColor = colors.pastelOrangeBg,
                accentColor = colors.pastelOrangeText,
                onClick = onNavigateToImages,
                modifier = Modifier.weight(1f)
            )

            PastelToolCard(
                title = "Resize",
                icon = Icons.Outlined.AspectRatio,
                backgroundColor = colors.pastelBlueBg,
                accentColor = colors.pastelBlueText,
                onClick = onNavigateToResize,
                modifier = Modifier.weight(1f)
            )

            PastelToolCard(
                title = "Convert",
                icon = Icons.Outlined.SwapHoriz,
                backgroundColor = colors.pastelPurpleBg,
                accentColor = colors.pastelPurpleText,
                onClick = onNavigateToConvert,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun PastelToolCard(
    title: String,
    icon: ImageVector,
    backgroundColor: Color,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clip(ToolCardShape)
            .background(backgroundColor)
            .border(width = 1.dp, color = accentColor.copy(alpha = 0.20f), shape = ToolCardShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(ToolCardShape)
                    .background(Color.White.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                ),
                color = BharatFileThemeCustom.colors.textPrimary
            )
        }
    }
}
