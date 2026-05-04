package com.songs.player.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.songs.core.ui.theme.ThemePreview
import com.songs.feature.player.R

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun SongProgressTracker(
    sliderPosition: Float,
    onSliderPositionChange: (Long) -> Unit = {},
    duration: Long,
    formattedCurrentPosition: String,
    formattedRemainingTime: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        val sliderColors = SliderDefaults.colors(
            thumbColor = Color.White,
            activeTrackColor = Color.White.copy(alpha = .6f),
            inactiveTrackColor = Color.White.copy(alpha = .25f)
        )
        Slider(
            value = sliderPosition,
            onValueChange = { newPosition ->
                onSliderPositionChange((newPosition * duration).toLong())
            },
            modifier = Modifier.fillMaxWidth(),
            valueRange = 0f..1f,
            colors = sliderColors,
            thumb = {
                Image(
                    painter = painterResource(id = R.drawable.scrubber),
                    contentDescription = "Slider Scrubber",
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Fit,
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    colors = sliderColors,
                    sliderState = sliderState,
                    modifier = Modifier
                        .height(8.dp)
                        .layout { measurable, constraints ->
                            val extraPx = 8.dp.roundToPx()
                            val placeable = measurable.measure(
                                constraints.copy(maxWidth = constraints.maxWidth + extraPx * 2)
                            )
                            layout(constraints.maxWidth, placeable.height) {
                                placeable.placeRelative(-extraPx, 0)
                            }
                        },
                    thumbTrackGapSize = 0.dp,
                    trackInsideCornerSize = 4.dp,
                    drawStopIndicator = null,
                )
            }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 8.dp,
                    end = 8.dp,
                    bottom = 8.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formattedCurrentPosition,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Text(
                text = formattedRemainingTime,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    ThemePreview {
        SongProgressTracker(
            sliderPosition = 0.5f,
            duration = 240000L,
            formattedCurrentPosition = "2:00",
            formattedRemainingTime = "-2:00"
        )
    }
}

@Preview
@Composable
private fun PreviewStart() {
    ThemePreview {
        SongProgressTracker(
            sliderPosition = 0.0f,
            duration = 240000L,
            formattedCurrentPosition = "2:00",
            formattedRemainingTime = "-2:00"
        )
    }
}

@Preview
@Composable
private fun PreviewEnd() {
    ThemePreview {
        SongProgressTracker(
            sliderPosition = 1f,
            duration = 240000L,
            formattedCurrentPosition = "2:00",
            formattedRemainingTime = "-2:00"
        )
    }
}
