package com.voicerep.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = FitnessPrimary,
    secondary = FitnessSecondary,
    tertiary = FitnessAccent,
    background = SurfaceDark,
    surface = CardDark,
    onPrimary = TextPrimary,
    onSecondary = SurfaceDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = FitnessPrimary,
    secondary = FitnessSecondary,
    tertiary = FitnessAccent
)

@Composable
fun VoiceRepTheme(
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
