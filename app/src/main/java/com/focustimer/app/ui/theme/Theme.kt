package com.focustimer.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// A deliberate indigo/coral/teal identity instead of the Material baseline purple.
val WorkColor = Color(0xFF5B4FE9)
val RestColor = Color(0xFF00A896)

private val LightColors = lightColorScheme(
    primary = Color(0xFF5B4FE9),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE4E0FF),
    onPrimaryContainer = Color(0xFF180568),
    secondary = Color(0xFFFF7A59),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDBD1),
    onSecondaryContainer = Color(0xFF3A0D00),
    tertiary = Color(0xFF00A896),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF9EF2E5),
    onTertiaryContainer = Color(0xFF00201C),
    background = Color(0xFFFBF8FF),
    onBackground = Color(0xFF1B1B23),
    surface = Color(0xFFFBF8FF),
    onSurface = Color(0xFF1B1B23),
    surfaceVariant = Color(0xFFE5E0EC),
    onSurfaceVariant = Color(0xFF47454F),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    outline = Color(0xFF79747E)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFC3BFFF),
    onPrimary = Color(0xFF2A1B8F),
    primaryContainer = Color(0xFF4030B0),
    onPrimaryContainer = Color(0xFFE4E0FF),
    secondary = Color(0xFFFFB4A0),
    onSecondary = Color(0xFF5F1600),
    secondaryContainer = Color(0xFF7D2A0F),
    onSecondaryContainer = Color(0xFFFFDBD1),
    tertiary = Color(0xFF82D5C7),
    onTertiary = Color(0xFF00382F),
    tertiaryContainer = Color(0xFF005046),
    onTertiaryContainer = Color(0xFF9EF2E5),
    background = Color(0xFF131318),
    onBackground = Color(0xFFE5E1E9),
    surface = Color(0xFF131318),
    onSurface = Color(0xFFE5E1E9),
    surfaceVariant = Color(0xFF47454F),
    onSurfaceVariant = Color(0xFFC9C5D0),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    outline = Color(0xFF928F99)
)

@Composable
fun FocusTimerTheme(content: @Composable () -> Unit) {
    val colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
