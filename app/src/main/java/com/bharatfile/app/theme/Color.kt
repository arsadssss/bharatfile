package com.bharatfile.app.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary Gradient & Brand Colors
val ElectricBlue = Color(0xFF4E65FF)
val ElectricBlueDark = Color(0xFF384BD8)
val ElectricPurple = Color(0xFF7A5AF8)
val DeepIndigo = Color(0xFF3F3D56)
val WarningOrange = Color(0xFFF79009)
val WarningOrangeLight = Color(0xFFFEF0C7)

val BrandGradient = Brush.horizontalGradient(
    colors = listOf(ElectricBlue, ElectricPurple)
)

val BrandGlowBrush = Brush.radialGradient(
    colors = listOf(
        ElectricBlue.copy(alpha = 0.25f),
        Color.Transparent
    )
)

// Background & Surface
val BgLight = Color(0xFFF6F8FE)
val BgDark = Color(0xFF0F111A)

val CardSurfaceLight = Color(0xFFFFFFFF)
val CardSurfaceDark = Color(0xFF181B26)

val CardBorderLight = Color(0xFFE8ECF4)
val CardBorderDark = Color(0xFF2B3042)

val DashedBorderLight = Color(0xFFCFD5E2)
val DashedBorderDark = Color(0xFF3B4156)

// Text Colors
val TextPrimaryLight = Color(0xFF101828)
val TextPrimaryDark = Color(0xFFF2F4F7)

val TextSecondaryLight = Color(0xFF667085)
val TextSecondaryDark = Color(0xFF98A2B3)

// Status Colors
val SuccessGreen = Color(0xFF12B76A)
val SuccessGreenLight = Color(0xFFECFDF3)
val ErrorRed = Color(0xFFF04438)
val ErrorRedLight = Color(0xFFFEF3F2)

// Pastel Tool Card Colors (matching reference "You may also like")
val PastelOrangeBgLight = Color(0xFFFFF4EC)
val PastelOrangeText = Color(0xFFE07A10)

val PastelBlueBgLight = Color(0xFFEEF4FF)
val PastelBlueText = Color(0xFF2E90FA)

val PastelPurpleBgLight = Color(0xFFF6EEFD)
val PastelPurpleText = Color(0xFF8B5CF6)

val PastelGreenBgLight = Color(0xFFEDFDF8)
val PastelGreenText = Color(0xFF079455)

val PastelPinkBgLight = Color(0xFFFDF2F8)
val PastelPinkText = Color(0xFFD61A87)

// Dark equivalents for Pastel Cards
val PastelOrangeBgDark = Color(0xFF2A1C12)
val PastelBlueBgDark = Color(0xFF132038)
val PastelPurpleBgDark = Color(0xFF201633)
val PastelGreenBgDark = Color(0xFF0F261E)
val PastelPinkBgDark = Color(0xFF2B1320)
