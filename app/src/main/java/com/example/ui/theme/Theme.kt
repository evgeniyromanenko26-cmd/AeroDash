package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val AeroLightColorScheme = lightColorScheme(
    primary = AeroSkyDeep,
    onPrimary = Color.White,
    primaryContainer = AeroIceBlue,
    onPrimaryContainer = AeroInkPrimary,
    secondary = AeroEmeraldLime,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCFCE7),
    onSecondaryContainer = Color(0xFF064E3B),
    tertiary = AeroCoralOrange,
    onTertiary = Color.White,
    background = Color(0xFFD8F3FF),
    onBackground = AeroInkPrimary,
    surface = AeroGlassSurfaceHigh,
    onSurface = AeroInkPrimary,
    surfaceVariant = AeroGlassSurfaceMid,
    onSurfaceVariant = AeroInkSecondary,
    outline = AeroGlassBorderBright,
)

private val AeroDarkColorScheme = darkColorScheme(
    primary = AeroCyanGlow,
    onPrimary = AeroDeepNavy,
    primaryContainer = Color(0xFF0C4A6E),
    onPrimaryContainer = AeroIceBlue,
    secondary = AeroAquaMint,
    onSecondary = Color(0xFF022C22),
    tertiary = AeroSunAmber,
    onTertiary = Color(0xFF451A03),
    background = AeroDarkBackdrop,
    onBackground = Color.White,
    surface = AeroDarkGlass,
    onSurface = Color.White,
    surfaceVariant = Color(0x660E4970),
    onSurfaceVariant = Color(0xFFBAE6FD),
    outline = AeroGlassBorderSoft,
)

val AeroShapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

@Composable
fun AeroDashTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) AeroDarkColorScheme else AeroLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AeroTypography,
        shapes = AeroShapes,
        content = content,
    )
}
