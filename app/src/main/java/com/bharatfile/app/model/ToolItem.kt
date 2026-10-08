package com.bharatfile.app.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class ToolCategory {
    CORE,
    PDF_TOOLS,
    IMAGE_TOOLS,
    STUDENT_UTILITY
}

data class ToolItem(
    val id: String,
    val title: String,
    val description: String,
    val route: String,
    val category: ToolCategory,
    val isPopular: Boolean = false,
    val badge: String? = null
)
