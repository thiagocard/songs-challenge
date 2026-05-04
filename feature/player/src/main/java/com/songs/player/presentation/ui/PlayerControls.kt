package com.songs.player.presentation.ui

import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.songs.core.ui.theme.ThemePreview
import com.songs.feature.player.R

@Composable
internal fun PlayerControls(
    isPlaying: Boolean,
    isRepeatOn: Boolean,
    onPlayPauseClick: () -> Unit,
    onRewindClick: () -> Unit,
    onForwardClick: () -> Unit,
    onRepeatClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPlayPauseClick,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(32.dp)
                )
                .size(64.dp)
        ) {
            // avd_play_pause: start state = play icon, end state = pause icon.
            // Toggling atEnd triggers the morph animation automatically.
            val playPause =
                AnimatedImageVector.animatedVectorResource(R.drawable.avd_play_pause)
            val painter = rememberAnimatedVectorPainter(
                animatedImageVector = playPause,
                atEnd = isPlaying
            )
            Icon(
                painter = painter,
                contentDescription = if (isPlaying) stringResource(R.string.pause) else stringResource(
                    R.string.play
                ),
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }



        IconButton(
            onClick = onRewindClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_backward_bar_fill),
                contentDescription = stringResource(R.string.previous),
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }


        IconButton(
            onClick = onForwardClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_forward_bar_fill),
                contentDescription = stringResource(R.string.next),
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = onRepeatClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_play_on_repeat),
                contentDescription = stringResource(R.string.repeat),
                modifier = Modifier.size(32.dp),
                tint = if (isRepeatOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Preview
@Composable
private fun PreviewRepeatOn() {
    ThemePreview {
        PlayerControls(
            isPlaying = true,
            isRepeatOn = true,
            onPlayPauseClick = {},
            onRewindClick = {},
            onForwardClick = {},
            onRepeatClick = {},
        )
    }
}

@Preview
@Composable
private fun PreviewRepeatOff() {
    ThemePreview {
        PlayerControls(
            isPlaying = true,
            isRepeatOn = false,
            onPlayPauseClick = {},
            onRewindClick = {},
            onForwardClick = {},
            onRepeatClick = {},
        )
    }
}

@Preview
@Composable
private fun PreviewNotPlaying() {
    ThemePreview {
        PlayerControls(
            isPlaying = false,
            isRepeatOn = false,
            onPlayPauseClick = {},
            onRewindClick = {},
            onForwardClick = {},
            onRepeatClick = {},
        )
    }
}
