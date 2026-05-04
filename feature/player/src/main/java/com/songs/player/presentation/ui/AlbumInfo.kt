package com.songs.player.presentation.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.songs.core.ui.theme.ThemePreview
import com.songs.player.domain.model.Track

@Composable
internal fun AlbumInfo(
    currentTrack: Track?,
    @SuppressLint("ModifierParameter") titleModifier: Modifier = Modifier,
    subtitleModifier: Modifier = Modifier,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = currentTrack?.trackName.orEmpty(),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Start,
            modifier = titleModifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = currentTrack?.artistName.orEmpty(),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Start,
            modifier = subtitleModifier.fillMaxWidth()
        )
    }
}

@Preview
@Composable
private fun Preview(
    @PreviewParameter(TrackPreviewParameterProvider ::class) track: Track
) {
    ThemePreview {
        AlbumInfo(
            currentTrack = track
        )
    }
}
