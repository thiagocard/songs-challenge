package com.songs.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.songs.core.ui.R

enum class MediaItemComponentStyle {
    ROW, COLUMN;
}

/**
 * Base composable shared by [SongItemComponent] and [AlbumItemComponent].
 */
@Composable
internal fun MediaItemComponent(
    title: String,
    subtitle: String,
    artworkUrl: String?,
    artworkSize: Dp,
    artworkCornerRadius: Dp,
    style: MediaItemComponentStyle,
    titleStyle: @Composable () -> TextStyle,
    subtitleStyle: @Composable () -> TextStyle,
    modifier: Modifier = Modifier,
    artworkModifier: Modifier = Modifier,
    titleModifier: Modifier = Modifier,
    subtitleModifier: Modifier = Modifier,
    subtitleAlpha: Float = 1f,
    isClickable: Boolean,
    showNavOption: Boolean,
    isPlaying: Boolean,
    onMenuItemClick: (MediaDropdownMenuItemAction) -> Unit = {},
    onClick: () -> Unit = {},
) {
    when (style) {
        MediaItemComponentStyle.ROW -> RowMediaItemComponent(
            title = title,
            subtitle = subtitle,
            artworkUrl = artworkUrl,
            artworkSize = artworkSize,
            artworkCornerRadius = artworkCornerRadius,
            titleStyle = titleStyle,
            subtitleStyle = subtitleStyle,
            subtitleAlpha = subtitleAlpha,
            modifier = modifier,
            artworkModifier = artworkModifier,
            titleModifier = titleModifier,
            subtitleModifier = subtitleModifier,
            isClickable = isClickable,
            showNavOption = showNavOption,
            isPlaying = isPlaying,
            onClick = onClick,
            onMenuItemClick = onMenuItemClick,
        )

        MediaItemComponentStyle.COLUMN -> ColumnMediaItemComponent(
            title = title,
            subtitle = subtitle,
            artworkUrl = artworkUrl,
            artworkSize = artworkSize,
            artworkCornerRadius = artworkCornerRadius,
            titleStyle = titleStyle,
            subtitleStyle = subtitleStyle,
            subtitleAlpha = subtitleAlpha,
            modifier = modifier,
            isClickable = isClickable,
            isPlaying = isPlaying,
            onClick = onClick,
        )
    }
}

@Composable
fun RowMediaItemComponent(
    title: String,
    subtitle: String,
    artworkUrl: String?,
    artworkSize: Dp,
    artworkCornerRadius: Dp,
    titleStyle: @Composable () -> TextStyle,
    subtitleStyle: @Composable () -> TextStyle,
    subtitleAlpha: Float,
    modifier: Modifier = Modifier,
    artworkModifier: Modifier = Modifier,
    titleModifier: Modifier = Modifier,
    subtitleModifier: Modifier = Modifier,
    isClickable: Boolean = true,
    showNavOption: Boolean = true,
    isPlaying: Boolean = false,
    onClick: () -> Unit = {},
    onMenuItemClick: (MediaDropdownMenuItemAction) -> Unit = {},
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = isClickable, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!artworkUrl.isNullOrEmpty()) {
            AlbumCover(
                imageUrl = artworkUrl,
                cornerRadius = artworkCornerRadius,
                size = artworkSize,
                modifier = artworkModifier,
            )
        }

        AlbumTitleAndArtist(
            title = title,
            titleStyle = titleStyle,
            subtitle = subtitle,
            subtitleStyle = subtitleStyle,
            subtitleAlpha = subtitleAlpha,
            titleModifier = titleModifier,
            subtitleModifier = subtitleModifier,
        )

        if (showNavOption) {
            Box {
                MediaOptionsIconButton(
                    isMenuExpanded = isMenuExpanded,
                    onMenuClick = { isMenuExpanded = it },
                    onMenuItemClick = onMenuItemClick,
                )
            }
        }

        if (isPlaying) {
            Image(
                painter = painterResource(id = R.drawable.playing_animation_icon),
                contentDescription = stringResource(R.string.playing),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun ColumnMediaItemComponent(
    title: String,
    subtitle: String,
    artworkUrl: String?,
    artworkSize: Dp,
    artworkCornerRadius: Dp,
    titleStyle: @Composable () -> TextStyle,
    subtitleStyle: @Composable () -> TextStyle,
    subtitleAlpha: Float,
    modifier: Modifier = Modifier,
    isClickable: Boolean = true,
    isPlaying: Boolean = false,
    onClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = isClickable, onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (!artworkUrl.isNullOrEmpty()) {
            AlbumCover(
                imageUrl = artworkUrl,
                cornerRadius = artworkCornerRadius,
                size = artworkSize,
            )
        }

        AlbumTitleAndArtist(
            title = title,
            titleStyle = titleStyle,
            subtitle = subtitle,
            subtitleStyle = subtitleStyle,
            subtitleAlpha = subtitleAlpha
        )

        if (isPlaying) {
            Image(
                painter = painterResource(id = R.drawable.playing_animation_icon),
                contentDescription = stringResource(R.string.playing),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun ColumnScope.AlbumTitleAndArtist(
    title: String,
    titleStyle: @Composable (() -> TextStyle),
    subtitle: String,
    subtitleStyle: @Composable (() -> TextStyle),
    subtitleAlpha: Float
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = titleStyle(),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = subtitle,
            style = subtitleStyle(),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = subtitleAlpha),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun RowScope.AlbumTitleAndArtist(
    title: String,
    titleStyle: @Composable (() -> TextStyle),
    subtitle: String,
    subtitleStyle: @Composable (() -> TextStyle),
    subtitleAlpha: Float,
    titleModifier: Modifier = Modifier,
    subtitleModifier: Modifier = Modifier,
) {
    Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = title,
            style = titleStyle(),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = titleModifier,
        )
        Text(
            text = subtitle,
            style = subtitleStyle(),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = subtitleAlpha),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = subtitleModifier,
        )
    }
}

enum class MediaDropdownMenuItemAction {
    VIEW_ALBUM,
}

@Composable
private fun MediaOptionsIconButton(
    isMenuExpanded: Boolean,
    onMenuClick: (Boolean) -> Unit,
    onMenuItemClick: (MediaDropdownMenuItemAction) -> Unit,
) {
    IconButton(onClick = { onMenuClick(true) }) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = stringResource(R.string.menu),
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
        )
    }
    DropdownMenu(
        expanded = isMenuExpanded,
        onDismissRequest = { onMenuClick(false) }
    ) {
        DropdownMenuItem(
            text = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.view_album),
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = stringResource(R.string.chevron),
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            onClick = {
                onMenuClick(false) // Close the menu before navigating to avoid UI glitches
                onMenuItemClick(MediaDropdownMenuItemAction.VIEW_ALBUM)
            }
        )
    }
}
