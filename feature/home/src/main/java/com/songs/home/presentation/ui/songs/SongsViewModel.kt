package com.songs.home.presentation.ui.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.compose.LazyPagingItems
import com.songs.common.playback.NowPlayingProvider
import com.songs.home.domain.model.Song
import com.songs.home.domain.usecase.GetSongsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
internal class SongsViewModel @Inject constructor(
    getSongsUseCase: GetSongsUseCase,
    nowPlayingProvider: NowPlayingProvider,
) : ViewModel() {

    private val _searchTerm = MutableStateFlow("")
    private val searchTerm = _searchTerm
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            initialValue = "",
        )

    private val _events = MutableSharedFlow<SongsSideEffect>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    /**
     * Paging stream for the current (debounced) search term.
     * [cachedIn] keeps pages in memory across recompositions and config changes.
     * [flatMapLatest] cancels the previous stream when the term changes.
     */
    val pagingData: Flow<PagingData<Song>> = _searchTerm
        .debounce(DEBOUNCE_TIME)
        .map { it.ifBlank { DEFAULT_TERM } }
        .distinctUntilChanged()
        .flatMapLatest { term -> getSongsUseCase(term) }
        .cachedIn(viewModelScope)

    private val nowPlayingTrackId = nowPlayingProvider.nowPlaying
        .map { it?.trackId }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    val uiState: StateFlow<SongsUiState> = combine(
        searchTerm, nowPlayingTrackId,
    ) { term, trackId ->
        SongsUiState(
            searchTerm = term,
            nowPlayingTrackId = trackId,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SongsUiState(),
    )

    fun onSearchTermChanged(term: String) {
        _searchTerm.update { term }
    }

    fun resetToDefault() {
        _searchTerm.update { "" }
    }

    fun onSongClick(
        song: Song,
        pagingItems: LazyPagingItems<Song>,
    ) {
        onSongClick(
            song = song,
            snapshot = pagingItems.itemSnapshotList,
            itemCount = pagingItems.itemCount,
            peek = pagingItems::peek,
        )
    }

    @Suppress("VisibleForTests")
    internal fun onSongClick(
        song: Song,
        snapshot: Iterable<Song?>,
        itemCount: Int,
        peek: (Int) -> Song?,
    ) {
        val currentTrackId = song.trackId ?: return

        viewModelScope.launch {
            val currentIndex = snapshot.indexOfFirst { it?.trackId == currentTrackId }
            if (currentIndex < 0) return@launch

            val trackIds = (0 until itemCount)
                .mapNotNull { peek(it)?.trackId }
                .ifEmpty { listOf(currentTrackId) }

            _events.emit(SongsSideEffect.NavigateToPlayer(trackIds, currentTrackId))
        }
    }

    private companion object {
        const val DEBOUNCE_TIME = 300L
        const val DEFAULT_TERM = "rock"
    }
}
