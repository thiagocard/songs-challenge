package com.songs.home.presentation.ui.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.songs.home.domain.model.Song
import com.songs.home.domain.usecase.GetSongsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

private const val DEBOUNCE_TIME = 300L
private const val DEFAULT_TERM = "pop"

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SongsViewModel @Inject constructor(
    getSongsUseCase: GetSongsUseCase,
) : ViewModel() {

    private val _searchTerm = MutableStateFlow("")
    val searchTerm = _searchTerm.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        initialValue = "",
    )

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

    fun onSearchTermChanged(term: String) {
        _searchTerm.update { term }
    }

    fun resetToDefault() {
        _searchTerm.update { "" }
    }
}
