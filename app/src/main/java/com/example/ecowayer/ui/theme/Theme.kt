package com.example.ecowayer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBEA6FF),
    onPrimary = Color(0xFF241044),
    secondary = Color(0xFF8AD48E),
    background = Color(0xFF121416),
    onBackground = Color(0xFFF1F2F1),
    surface = Color(0xFF1C2022),
    onSurface = Color(0xFFF1F2F1),
    surfaceVariant = Color(0xFF292E31),
    onSurfaceVariant = Color(0xFFCCD0D0),
    outlineVariant = Color(0xFF454B4E)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF7045BC),
    onPrimary = Color.White,
    secondary = Color(0xFF3F9A48),
    background = Color(0xFFF6F7F9),
    onBackground = Color(0xFF1C1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1C1C1E),
    surfaceVariant = Color(0xFFF2F1F6),
    onSurfaceVariant = Color(0xFF58665C),
    outlineVariant = Color(0xFFE4E4E7)

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun EcowayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
