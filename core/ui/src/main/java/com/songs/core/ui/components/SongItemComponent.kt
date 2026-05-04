package com.songs.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.songs.core.ui.theme.PhonePreviews
import com.songs.core.ui.theme.ThemePreview

@Composable
fun SongItemComponent(
    title: String,
    subtitle: String,
    artworkUrl: String?,
    modifier: Modifier = Modifier,
    artworkModifier: Modifier = Modifier,
    titleModifier: Modifier = Modifier,
    subtitleModifier: Modifier = Modifier,
    showNavOption: Boolean = true,
    isPlaying: Boolean = false,
    onMenuClick: (MediaDropdownMenuItemAction) -> Unit = {},
    onClick: () -> Unit = {},
) {
    MediaItemComponent(
        title = title,
        subtitle = subtitle,
        artworkUrl = artworkUrl,
        artworkSize = 78.dp,
        artworkCornerRadius = 4.dp,
        titleStyle = { MaterialTheme.typography.bodyMedium },
        subtitleStyle = { MaterialTheme.typography.bodySmall },
        subtitleAlpha = 0.5f,
        modifier = modifier,
        artworkModifier = artworkModifier,
        titleModifier = titleModifier,
        subtitleModifier = subtitleModifier,
        showNavOption = showNavOption,
        isPlaying = isPlaying,
        onMenuItemClick = onMenuClick,
        onClick = onClick,
        style = MediaItemComponentStyle.ROW,
        isClickable = true,
    )
}

@PhonePreviews
@Composable
private fun Preview() {
    ThemePreview {
        Column {
            SongItemComponent(
                title = "Bohemian Rhapsody",
                subtitle = "Queen",
                artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/42/20/88/422088ba-d965-7eaa-cd51-e10a4a77e74e/00028948099126.jpg/500x500bb.jpg",
                isPlaying = false
            )
            SongItemComponent(
                title = "Stairway to Heaven",
                subtitle = "Led Zeppelin",
                artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/f3/2f/6c/f32f6c2e-d965-7eaa-cd51-e10a4a77e74e/00008902099126.jpg/500x500bb.jpg",
                isPlaying = true
            )
        }
    }
}
