package com.songs.core.ui.resources

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

@Suppress("MagicNumber")
object Colors {

    // Dark Theme Colors
    internal val PureBlack = Color(0xFF000000)
    internal val TealAccent = Color(0xFF0086A0)
    val DarkGray = Color(0xFF1A1A1A)
    internal val LightGray = Color(0xFFE0E0E0)

    // Dark Theme
    val DarkColors = darkColorScheme(
        primary = TealAccent,
        onPrimary = PureBlack,
        secondary = TealAccent,
        onSecondary = PureBlack,
        background = PureBlack,
        onBackground = LightGray,
        surface = DarkGray,
        onSurface = LightGray,
        onSurfaceVariant = TealAccent,
    )
}
