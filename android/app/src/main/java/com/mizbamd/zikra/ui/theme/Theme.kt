package com.mizbamd.zikra.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Forest = Color(0xFF16352F)
val ForestDark = Color(0xFF0F241F)
val ForestMid = Color(0xFF1E463D)
val Gold = Color(0xFFC9A24A)
val GoldLight = Color(0xFFE4C878)
val Cream = Color(0xFFF6F1E6)
val Ink = Color(0xFF14241F)
val CardWhite = Color(0xFFFFFDF8)

private val ZikraColors = lightColorScheme(
    primary = Gold,
    onPrimary = ForestDark,
    secondary = ForestMid,
    onSecondary = Cream,
    tertiary = GoldLight,
    background = Forest,
    onBackground = Cream,
    surface = CardWhite,
    onSurface = Ink,
    surfaceVariant = Cream,
    onSurfaceVariant = Ink,
    outline = Gold.copy(alpha = 0.55f),
    error = Color(0xFFB54A4A),
    onError = Color.White,
)

private val ZikraTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium,
        fontSize = 44.sp,
        lineHeight = 52.sp,
        color = Cream,
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium,
        fontSize = 32.sp,
        color = Cream,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        color = Ink,
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = Ink,
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = Ink,
    ),
)

@Composable
fun ZikraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ZikraColors,
        typography = ZikraTypography,
        content = content,
    )
}
