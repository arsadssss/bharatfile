package com.bharatfile.app.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Immutable
data class BharatFileExtendedColors(
    val brandGradient: Brush,
    val cardSurface: Color,
    val cardBorder: Color,
    val dashedBorder: Color,
    val segmentedContainer: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val pastelOrangeBg: Color,
    val pastelBlueBg: Color,
    val pastelPurpleBg: Color,
    val pastelGreenBg: Color,
    val pastelOrangeText: Color,
    val pastelBlueText: Color,
    val pastelPurpleText: Color,
    val pastelGreenText: Color,
    val success: Color,
    val error: Color,
    val glowColor: Color
)

val LocalBharatFileColors = staticCompositionLocalOf {
    BharatFileExtendedColors(
        brandGradient = BrandGradient,
        cardSurface = CardSurfaceLight,
        cardBorder = CardBorderLight,
        dashedBorder = DashedBorderLight,
        segmentedContainer = Color(0xFFF0F3FA),
        textPrimary = TextPrimaryLight,
        textSecondary = TextSecondaryLight,
        pastelOrangeBg = PastelOrangeBgLight,
        pastelBlueBg = PastelBlueBgLight,
        pastelPurpleBg = PastelPurpleBgLight,
        pastelGreenBg = PastelGreenBgLight,
        pastelOrangeText = PastelOrangeText,
        pastelBlueText = PastelBlueText,
        pastelPurpleText = PastelPurpleText,
        pastelGreenText = PastelGreenText,
        success = SuccessGreen,
        error = ErrorRed,
        glowColor = ElectricBlue.copy(alpha = 0.15f)
    )
}

private val LightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    secondary = ElectricPurple,
    onSecondary = Color.White,
    background = BgLight,
    onBackground = TextPrimaryLight,
    surface = CardSurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFF0F3FA),
    onSurfaceVariant = TextSecondaryLight,
    outline = CardBorderLight
)

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    secondary = ElectricPurple,
    onSecondary = Color.White,
    background = BgDark,
    onBackground = TextPrimaryDark,
    surface = CardSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF222634),
    onSurfaceVariant = TextSecondaryDark,
    outline = CardBorderDark
)

@Composable
fun BharatFileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val extendedColors = if (darkTheme) {
        BharatFileExtendedColors(
            brandGradient = BrandGradient,
            cardSurface = CardSurfaceDark,
            cardBorder = CardBorderDark,
            dashedBorder = DashedBorderDark,
            segmentedContainer = Color(0xFF1E2230),
            textPrimary = TextPrimaryDark,
            textSecondary = TextSecondaryDark,
            pastelOrangeBg = PastelOrangeBgDark,
            pastelBlueBg = PastelBlueBgDark,
            pastelPurpleBg = PastelPurpleBgDark,
            pastelGreenBg = PastelGreenBgDark,
            pastelOrangeText = Color(0xFFFFB066),
            pastelBlueText = Color(0xFF70B4FF),
            pastelPurpleText = Color(0xFFC4A2FF),
            pastelGreenText = Color(0xFF47D68F),
            success = SuccessGreen,
            error = ErrorRed,
            glowColor = ElectricPurple.copy(alpha = 0.25f)
        )
    } else {
        BharatFileExtendedColors(
            brandGradient = BrandGradient,
            cardSurface = CardSurfaceLight,
            cardBorder = CardBorderLight,
            dashedBorder = DashedBorderLight,
            segmentedContainer = Color(0xFFF0F3FA),
            textPrimary = TextPrimaryLight,
            textSecondary = TextSecondaryLight,
            pastelOrangeBg = PastelOrangeBgLight,
            pastelBlueBg = PastelBlueBgLight,
            pastelPurpleBg = PastelPurpleBgLight,
            pastelGreenBg = PastelGreenBgLight,
            pastelOrangeText = PastelOrangeText,
            pastelBlueText = PastelBlueText,
            pastelPurpleText = PastelPurpleText,
            pastelGreenText = PastelGreenText,
            success = SuccessGreen,
            error = ErrorRed,
            glowColor = ElectricBlue.copy(alpha = 0.15f)
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalBharatFileColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}

object BharatFileThemeCustom {
    val colors: BharatFileExtendedColors
        @Composable
        get() = LocalBharatFileColors.current
}
