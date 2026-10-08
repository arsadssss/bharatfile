package com.bharatfile.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bharatfile.app.data.HistoryManager
import com.bharatfile.app.data.PreferencesManager
import com.bharatfile.app.engine.FileCleaner
import com.bharatfile.app.model.CompressionMode
import com.bharatfile.app.model.ProcessedFileResult
import com.bharatfile.app.navigation.Screen
import com.bharatfile.app.theme.BharatFileThemeCustom
import com.bharatfile.app.theme.ElectricBlue
import com.bharatfile.app.theme.PillShape
import com.bharatfile.app.theme.SuccessGreen
import com.bharatfile.app.ui.components.GlassCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToRoute: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager.getInstance(context) }
    val historyManager = remember { HistoryManager.getInstance(context) }
    val colors = BharatFileThemeCustom.colors
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var defaultPdfPerc by remember { mutableStateOf(prefs.defaultPdfPercentage) }
    var defaultImgPerc by remember { mutableStateOf(prefs.defaultImagePercentage) }
    var defaultMode by remember { mutableStateOf(prefs.defaultMode) }

    var cacheSize by remember { mutableStateOf(FileCleaner.getCacheSizeBytes(context)) }
    var showClearHistoryConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Section 1: Account & Profile
            SettingsSectionHeader(title = "Account")
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToProfile),
                elevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ElectricBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Profile & Account Info",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Manage name, password, email, and stats",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = colors.textSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Default Compression Preferences
            SettingsSectionHeader(title = "Default Compression Preferences")
            GlassCard(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    // Default PDF Compression Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.FolderZip, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Default PDF Reduction", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = colors.textPrimary)
                        }
                        Text(text = "$defaultPdfPerc%", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = ElectricBlue)
                    }
                    Slider(
                        value = defaultPdfPerc.toFloat(),
                        onValueChange = {
                            defaultPdfPerc = it.toInt()
                            prefs.defaultPdfPercentage = defaultPdfPerc
                        },
                        valueRange = 1f..100f,
                        colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = ElectricBlue)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Default Image Compression Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Image, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Default Image Reduction", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = colors.textPrimary)
                        }
                        Text(text = "$defaultImgPerc%", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = ElectricBlue)
                    }
                    Slider(
                        value = defaultImgPerc.toFloat(),
                        onValueChange = {
                            defaultImgPerc = it.toInt()
                            prefs.defaultImagePercentage = defaultImgPerc
                        },
                        valueRange = 1f..100f,
                        colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = ElectricBlue)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Default Mode
                    Text(
                        text = "Preferred Compression Mode",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(PillShape)
                            .background(colors.segmentedContainer)
                            .padding(3.dp)
                    ) {
                        CompressionMode.values().forEach { mode ->
                            val isSelected = defaultMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(PillShape)
                                    .background(if (isSelected) colors.cardSurface else Color.Transparent)
                                    .clickable {
                                        defaultMode = mode
                                        prefs.defaultMode = mode
                                        scope.launch { snackbarHostState.showSnackbar("Default mode saved!") }
                                    }
                                    .padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) ElectricBlue else colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 3: Appearance
            SettingsSectionHeader(title = "Appearance")
            GlassCard(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onToggleDarkMode)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.DarkMode, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Dark Mode", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = colors.textPrimary)
                        Text(text = if (isDarkMode) "Enabled" else "Disabled", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = colors.textSecondary)
                    }
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(colors.segmentedContainer)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(text = if (isDarkMode) "Switch to Light" else "Switch to Dark", fontSize = 11.sp, color = ElectricBlue, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 4: Storage & Cache Cleaner
            SettingsSectionHeader(title = "Storage & Cache")
            GlassCard(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    // Clean Cache Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.CleaningServices, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "Temporary Cache", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = colors.textPrimary)
                                Text(text = "Cache: ${ProcessedFileResult.formatFileSize(cacheSize)}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = colors.textSecondary)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(colors.segmentedContainer)
                                .clickable {
                                    val cleared = FileCleaner.clearCache(context)
                                    cacheSize = FileCleaner.getCacheSizeBytes(context)
                                    scope.launch { snackbarHostState.showSnackbar("Cleared ${ProcessedFileResult.formatFileSize(cleared)} temporary cache!") }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = "Clear Cache", fontSize = 11.sp, color = ElectricBlue, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Clear History Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.DeleteSweep, contentDescription = null, tint = colors.error, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "Operation History", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = colors.textPrimary)
                                Text(text = "Erase saved log entries", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = colors.textSecondary)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(colors.error.copy(alpha = 0.12f))
                                .clickable { showClearHistoryConfirm = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = "Clear History", fontSize = 11.sp, color = colors.error, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 5: Privacy & Security
            SettingsSectionHeader(title = "Privacy & Legal")
            GlassCard(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SuccessGreen.copy(alpha = 0.12f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Security, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "100% On-Device Privacy Guaranteed. No files ever leave your phone.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                            color = SuccessGreen
                        )
                    }

                    SettingsLinkRow(icon = Icons.Outlined.Policy, title = "Privacy Policy", onClick = { onNavigateToRoute(Screen.Privacy.route) })
                    SettingsLinkRow(icon = Icons.Outlined.Description, title = "Terms of Service", onClick = { onNavigateToRoute(Screen.Terms.route) })
                    SettingsLinkRow(icon = Icons.Outlined.Email, title = "Support & Feedback", onClick = { onNavigateToRoute(Screen.Contact.route) })
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 6: About BharatFile
            SettingsSectionHeader(title = "About BharatFile")
            GlassCard(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(text = "BharatFile v1.0.0 (Production)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Engineered for Indian students, teachers, job aspirants, and professionals.", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = colors.textSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Made with ❤️ in India", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = ElectricBlue)
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }

        if (showClearHistoryConfirm) {
            AlertDialog(
                onDismissRequest = { showClearHistoryConfirm = false },
                title = { Text("Clear All History?", fontWeight = FontWeight.Bold) },
                text = { Text("All saved operation metadata will be cleared.") },
                confirmButton = {
                    TextButton(onClick = {
                        historyManager.clearHistory()
                        showClearHistoryConfirm = false
                        scope.launch { snackbarHostState.showSnackbar("History cleared!") }
                    }) {
                        Text("Clear", color = colors.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearHistoryConfirm = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    val colors = BharatFileThemeCustom.colors
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        ),
        color = colors.textSecondary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsLinkRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    val colors = BharatFileThemeCustom.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp), color = colors.textPrimary, modifier = Modifier.weight(1f))
        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(14.dp))
    }
}
