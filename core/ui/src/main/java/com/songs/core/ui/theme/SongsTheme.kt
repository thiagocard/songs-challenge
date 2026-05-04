package com.songs.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.songs.core.ui.resources.AppTypography
import com.songs.core.ui.resources.Colors

@Composable
fun SongsTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = Colors.DarkColors,
        typography = AppTypography,
        content = content,
    )
}
