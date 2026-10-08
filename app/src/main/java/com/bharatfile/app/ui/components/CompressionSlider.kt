package com.bharatfile.app.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bharatfile.app.model.CompressionLevel
import com.bharatfile.app.theme.BharatFileThemeCustom
import com.bharatfile.app.theme.ElectricBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompressionSlider(
    currentLevel: CompressionLevel,
    onLevelChange: (CompressionLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = BharatFileThemeCustom.colors
    val sliderValue = when (currentLevel) {
        CompressionLevel.LOW -> 0f
        CompressionLevel.MEDIUM -> 0.5f
        CompressionLevel.HIGH -> 1f
    }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        elevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Compression Level",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Slider
            Slider(
                value = sliderValue,
                onValueChange = { value ->
                    val newLevel = when {
                        value < 0.25f -> CompressionLevel.LOW
                        value > 0.75f -> CompressionLevel.HIGH
                        else -> CompressionLevel.MEDIUM
                    }
                    onLevelChange(newLevel)
                },
                steps = 1,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = ElectricBlue,
                    inactiveTrackColor = colors.cardBorder,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Labels Row: Low / Medium / High
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LevelLabel(
                    text = "Low",
                    isSelected = currentLevel == CompressionLevel.LOW,
                    onClick = { onLevelChange(CompressionLevel.LOW) }
                )
                LevelLabel(
                    text = "Medium",
                    isSelected = currentLevel == CompressionLevel.MEDIUM,
                    onClick = { onLevelChange(CompressionLevel.MEDIUM) }
                )
                LevelLabel(
                    text = "High",
                    isSelected = currentLevel == CompressionLevel.HIGH,
                    onClick = { onLevelChange(CompressionLevel.HIGH) }
                )
            }
        }
    }
}

@Composable
private fun LevelLabel(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = BharatFileThemeCustom.colors
    val interactionSource = remember { MutableInteractionSource() }

    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        ),
        color = if (isSelected) ElectricBlue else colors.textSecondary,
        modifier = Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    )
}
