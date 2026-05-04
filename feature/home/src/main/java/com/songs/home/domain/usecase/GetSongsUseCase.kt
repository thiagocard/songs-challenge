package com.songs.home.domain.usecase

import androidx.paging.PagingData
import com.songs.home.data.SongsRepository
import com.songs.home.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface GetSongsUseCase {
    operator fun invoke(term: String = "pop"): Flow<PagingData<Song>>
}

class GetSongsUseCaseImpl @javax.inject.Inject constructor(
    private val songsRepository: SongsRepository,
) : GetSongsUseCase {
    override fun invoke(term: String): Flow<PagingData<Song>> =
        songsRepository.getSongsPagingFlow(term)
}
