package com.songs.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.songs.core.ui.theme.AllPreviews
import com.songs.core.ui.theme.ThemePreview
import com.songs.core.ui.util.isLargeScreen

@Composable
fun AlbumItemComponent(
    title: String,
    subtitle: String,
    artworkUrl: String?,
    style: MediaItemComponentStyle,
    modifier: Modifier = Modifier,
) {
    val isLargeScreen = isLargeScreen()
    MediaItemComponent(
        title = title,
        subtitle = subtitle,
        artworkUrl = artworkUrl,
        artworkSize = 120.dp,
        artworkCornerRadius = 8.dp,
        titleStyle = {
            if (isLargeScreen) {
                MaterialTheme.typography.headlineLarge
            } else {
                MaterialTheme.typography.headlineSmall
            }
        },
        subtitleStyle = {
            if (isLargeScreen) {
                MaterialTheme.typography.bodyLarge
            } else {
                MaterialTheme.typography.bodyMedium
            }
        },
        style = style,
        modifier = modifier,
        isClickable = false,
        showNavOption = false,
        isPlaying = false,
    )
}

@AllPreviews
@Composable
private fun PreviewRowStyle() {
    ThemePreview {
        AlbumItemComponent(
            title = "Album Header",
            subtitle = "Artist Name",
            artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/f3/2f/6c/f32f6c2e-d965-7eaa-cd51-e10a4a77e74e/00008902099126.jpg/500x500bb.jpg",
            style = MediaItemComponentStyle.ROW,
        )
    }
}


@AllPreviews
@Composable
private fun PreviewColumnStyle() {
    ThemePreview {
        AlbumItemComponent(
            title = "Album Header",
            subtitle = "Artist Name",
            artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/f3/2f/6c/f32f6c2e-d965-7eaa-cd51-e10a4a77e74e/00008902099126.jpg/500x500bb.jpg",
            style = MediaItemComponentStyle.COLUMN,
        )
    }
}
