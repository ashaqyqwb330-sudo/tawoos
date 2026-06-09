package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkColorScheme = darkColorScheme(
    primary = GoldenBrass,
    secondary = TacticalGreen,
    tertiary = LightNavy,
    background = DarkNavy,
    surface = CardBackground,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = SoftGray,
    onSurface = SoftGray,
    error = CoralRed
)

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1B3A5C),
    secondary = Color(0xFF4A6FA5),
    background = Color(0xFFF4F6FA),
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    error = CoralRed
)

val MilitaryNeonColorScheme = darkColorScheme(
    primary = Color(0xFF00FFAA),    // أخضر نيون
    secondary = Color(0xFF00BFFF),  // أزرق نيون
    background = Color(0xFF0A0E17),
    surface = Color(0xFF121926),
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    error = CoralRed
)

@Composable
fun MyApplicationTheme(
    themeIndex: Int = 0, // 0 = Dark, 1 = Light, 2 = Military Neon
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeIndex) {
        0 -> DarkColorScheme
        1 -> LightColorScheme
        2 -> MilitaryNeonColorScheme
        else -> DarkColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
