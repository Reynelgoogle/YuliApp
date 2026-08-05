package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ToddleYellow,
    secondary = ToddleBlue,
    tertiary = ToddleGreen,
    background = ToddleBackground,
    surface = ToddleSurface,
    onPrimary = Color(0xFF0B1D3A),
    onSecondary = Color(0xFF0B1D3A),
    onTertiary = Color(0xFF0B1D3A),
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = ToddleYellow,
    secondary = ToddleBlue,
    tertiary = ToddleGreen,
    background = ToddleBackground,
    surface = ToddleSurface,
    onPrimary = Color(0xFF0B1D3A),
    onSecondary = Color(0xFF0B1D3A), // high contrast on yellow
    onTertiary = Color(0xFF0B1D3A),  // high contrast on cyan
    onBackground = Color.White,      // white on deep navy background
    onSurface = Color.White          // white on electric navy surface
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // We disable dynamic color by default for YuliApp to ensure the app is 
    // consistently colorful, inviting, and high-contrast for infant visual sensory systems.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
