package com.privatecloset.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PrivateClosetColorScheme = darkColorScheme(
    primary = Purple,
    onPrimary = TextPrimary,
    secondary = Gold,
    onSecondary = BgBlack,
    background = BgBlack,
    onBackground = TextPrimary,
    surface = BgCard,
    onSurface = TextPrimary,
    surfaceVariant = BgCardAlt,
    outline = BgStroke,
    error = Color(0xFFEF4444),
)

@Composable
fun PrivateClosetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PrivateClosetColorScheme,
        typography = PrivateClosetTypography,
        content = content
    )
}
