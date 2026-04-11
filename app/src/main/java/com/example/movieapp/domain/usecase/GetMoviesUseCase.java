package com.example.movieapp.domain.usecase;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.movieapp.common.MovieEntityMapper;
import com.example.movieapp.domain.models.MovieUi;
import com.example.movieapp.domain.repoInterfaces.MovieDBRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class GetMoviesUseCase {

    private final MovieDBRepository repository;

    @Inject
    public GetMoviesUseCase(MovieDBRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns LiveData<List<MovieUi>> mapped from Room entities.
     * Transformations.map() is lifecycle-aware and runs on the main thread.
     */
    public LiveData<List<MovieUi>> execute() {
        return Transformations.map(
                repository.getMovies(),
                MovieEntityMapper::mapToUiMovieList
        );
    }
    public LiveData<List<MovieUi>> executeFavoritesOnly() {
        return Transformations.map(
                repository.getFavorites(),
                MovieEntityMapper::mapToUiMovieList
        );
    }

//    public Single<List<MovieUi>> getFavoriteMoviesOffline(){
//        return repository.getFavoritesSingle()
//                .map(movieEntities ->
//                    movieEntities.stream()
//                            .map(MovieEntityMapper::mapToMovieUi)
//                            .collect(java.util.stream.Collectors.toList())
//                );
//    }

    /**
     * Triggers network refresh. Returns Completable so the ViewModel
     * can subscribe, handle errors, and clear the disposable in onCleared().
     */
    public Completable refresh() {
        return repository.refreshMovies();
    }

    /**
     * Atomic toggle by ID — no entity needed, no race condition.
     */
    public Completable toggleFavorite(int movieId) {
        return repository.toggleFavorite(movieId);
    }
}
