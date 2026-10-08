package com.bharatfile.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bharatfile.app.theme.BharatFileThemeCustom
import com.bharatfile.app.theme.ElectricBlue
import com.bharatfile.app.theme.ElectricBlueDark
import com.bharatfile.app.theme.PillShape
import com.bharatfile.app.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomCompressionSlider(
    percentage: Int,
    onPercentageChange: (Int) -> Unit,
    onSaveAsDefault: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = BharatFileThemeCustom.colors
    var savedFeedback by remember { mutableStateOf(false) }

    val levelDescription = when {
        percentage < 30 -> "Light compression • Maximum quality"
        percentage < 70 -> "Balanced compression • Recommended for forms"
        else -> "Aggressive compression • Minimum file size"
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
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            // Header Row: "Compression" + "%" badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Compression",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = levelDescription,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = colors.textSecondary
                    )
                }

                // Selected percentage prominent gradient badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(ElectricBlue, ElectricBlueDark)
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Continuous 1% to 100% Slider
            Slider(
                value = percentage.toFloat(),
                onValueChange = { onPercentageChange(it.toInt().coerceIn(1, 100)) },
                valueRange = 1f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = ElectricBlue,
                    inactiveTrackColor = colors.cardBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Scale Markers: 1% ---------------- 50% ---------------- 100%
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "1% (Max Quality)",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = colors.textSecondary
                )
                Text(
                    text = "50%",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = colors.textSecondary
                )
                Text(
                    text = "100% (Min Size)",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = colors.textSecondary
                )
            }

            // Quick preset percentage chips & "Set as Default" button
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Preset chips: 25%, 50%, 75%, 90%
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(25, 50, 75, 90).forEach { preset ->
                        val isSelected = percentage == preset
                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(if (isSelected) ElectricBlue.copy(alpha = 0.15f) else colors.segmentedContainer)
                                .clickable { onPercentageChange(preset) }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$preset%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                color = if (isSelected) ElectricBlue else colors.textPrimary
                            )
                        }
                    }
                }

                // "Set Default" chip
                if (onSaveAsDefault != null) {
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(if (savedFeedback) SuccessGreen.copy(alpha = 0.15f) else colors.segmentedContainer)
                            .clickable {
                                onSaveAsDefault()
                                savedFeedback = true
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (savedFeedback) Icons.Outlined.Check else Icons.Outlined.BookmarkBorder,
                                contentDescription = null,
                                tint = if (savedFeedback) SuccessGreen else colors.textSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (savedFeedback) "Default Saved" else "Set Default",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = if (savedFeedback) SuccessGreen else colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
