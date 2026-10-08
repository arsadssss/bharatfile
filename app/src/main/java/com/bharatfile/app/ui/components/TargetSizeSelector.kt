package com.bharatfile.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bharatfile.app.model.CompressionConfig
import com.bharatfile.app.model.CompressionMode
import com.bharatfile.app.model.ProcessedFileResult
import com.bharatfile.app.model.SizeUnit
import com.bharatfile.app.theme.BharatFileThemeCustom
import com.bharatfile.app.theme.ElectricBlue
import com.bharatfile.app.theme.PillShape
import com.bharatfile.app.theme.WarningOrange

@Composable
fun TargetSizeSelector(
    config: CompressionConfig,
    originalSizeBytes: Long?,
    onConfigChange: (CompressionConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = BharatFileThemeCustom.colors
    val scrollState = rememberScrollState()

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
            // Mode Selector Tabs: [ Reduce by % ] [ Target final size ]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(PillShape)
                    .background(colors.segmentedContainer)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CompressionMode.values().forEach { mode ->
                    val isSelected = config.mode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(PillShape)
                            .background(if (isSelected) colors.cardSurface else Color.Transparent)
                            .clickable { onConfigChange(config.copy(mode = mode)) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.displayName,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            color = if (isSelected) ElectricBlue else colors.textSecondary
                        )
                    }
                }
            }

            // Target Size Input Section (when in TARGET_SIZE mode)
            AnimatedVisibility(
                visible = config.mode == CompressionMode.TARGET_SIZE,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Text(
                        text = "Exact Target File Size",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Engine will optimize to get as close as possible without exceeding",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Input Field + Unit Picker [ KB / MB ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = config.targetValue,
                            onValueChange = { newValue ->
                                val filtered = newValue.filter { it.isDigit() || it == '.' }
                                onConfigChange(config.copy(targetValue = filtered))
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            placeholder = { Text("e.g. 200", fontSize = 13.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = colors.cardBorder,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        // Unit Switcher [ KB ] [ MB ]
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.segmentedContainer)
                                .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                                .padding(3.dp)
                        ) {
                            SizeUnit.values().forEach { unit ->
                                val isSelected = config.targetUnit == unit
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(if (isSelected) ElectricBlue else Color.Transparent)
                                        .clickable { onConfigChange(config.copy(targetUnit = unit)) }
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = unit.label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        ),
                                        color = if (isSelected) Color.White else colors.textPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Preset Chips (50 KB, 100 KB, 200 KB, 500 KB, 1 MB, 2 MB, 5 MB, 10 MB)
                    Text(
                        text = "Quick Presets for Exam & Govt Portals:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        color = colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CompressionConfig.QUICK_PRESETS.forEach { (presetVal, presetUnit) ->
                            val isSelected = config.targetValue == presetVal && config.targetUnit == presetUnit
                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(if (isSelected) ElectricBlue.copy(alpha = 0.15f) else colors.segmentedContainer)
                                    .border(
                                        width = if (isSelected) 1.dp else 0.dp,
                                        color = if (isSelected) ElectricBlue else Color.Transparent,
                                        shape = PillShape
                                    )
                                    .clickable {
                                        onConfigChange(config.copy(targetValue = presetVal, targetUnit = presetUnit))
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$presetVal ${presetUnit.label}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    ),
                                    color = if (isSelected) ElectricBlue else colors.textPrimary
                                )
                            }
                        }
                    }

                    // Warning / Validation Banner if target > original file size
                    if (originalSizeBytes != null && originalSizeBytes > 0) {
                        val targetBytes = config.targetBytes
                        if (targetBytes != null && targetBytes >= originalSizeBytes) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(WarningOrange.copy(alpha = 0.12f))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.WarningAmber,
                                    contentDescription = null,
                                    tint = WarningOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Target is larger than original (${ProcessedFileResult.formatFileSize(originalSizeBytes)}). Consider entering a smaller size to reduce.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = WarningOrange
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
