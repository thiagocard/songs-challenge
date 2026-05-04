package com.songs.player.presentation.ui

import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.songs.feature.player.R

/**
 * Animated play/pause button using the AVD morph drawable.
 *
 * @param isPlaying  When true the button shows the pause icon (and tapping will pause).
 * @param onClick    Called when the button is tapped.
 * @param buttonSize Overall tappable size of the button. Defaults to 64 dp (full-size player).
 * @param iconSize   Size of the icon inside the button. Defaults to 32 dp.
 * @param showBackground Whether to draw the rounded surface background. Defaults to true.
 */
@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun PlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonSize: Dp = 64.dp,
    iconSize: Dp = 32.dp,
    showBackground: Boolean = true,
) {
    val backgroundModifier = if (showBackground) {
        Modifier.background(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(32.dp),
        )
    } else Modifier

    IconButton(
        onClick = onClick,
        modifier = modifier
            .then(backgroundModifier)
            .size(buttonSize),
    ) {
        val playPause = AnimatedImageVector.animatedVectorResource(R.drawable.avd_play_pause)
        val painter = rememberAnimatedVectorPainter(
            animatedImageVector = playPause,
            atEnd = isPlaying,
        )
        Icon(
            painter = painter,
            contentDescription = if (isPlaying) stringResource(R.string.pause)
                                 else stringResource(R.string.play),
            modifier = Modifier.size(iconSize),
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}
