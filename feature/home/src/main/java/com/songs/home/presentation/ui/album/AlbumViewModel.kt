package com.songs.home.presentation.ui.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.songs.home.domain.usecase.GetSongsByAlbumUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = AlbumViewModel.Factory::class)
internal class AlbumViewModel @AssistedInject constructor(
    private val getSongsByAlbumUseCase: GetSongsByAlbumUseCase,
    @Assisted private val albumId: String,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(albumId: String): AlbumViewModel
    }

    private val _uiState = MutableStateFlow<AlbumUiState>(AlbumUiState.Loading)
    val uiState = _uiState.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        initialValue = _uiState.value
    )

    init {
        loadAlbumSongs()
    }

    private fun loadAlbumSongs() {
        viewModelScope.launch {
            try {
                getSongsByAlbumUseCase(albumId)
                    .collect { albumWithSongs ->
                        if (albumWithSongs.songs.isNotEmpty()) {
                            _uiState.update {
                                AlbumUiState.Success(albumWithSongs)
                            }
                        } else {
                            _uiState.update { AlbumUiState.Error }
                        }
                    }
            } catch (_: Exception) {
                _uiState.update { AlbumUiState.Error }
            }
        }
    }
}
