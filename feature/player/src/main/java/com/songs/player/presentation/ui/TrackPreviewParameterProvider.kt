package com.songs.player.presentation.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.songs.player.domain.model.Track
import com.songs.support.mock.TrackMock

class TrackPreviewParameterProvider : PreviewParameterProvider<Track> {
    override val values = TrackMock.trackList.asSequence()
}
