package com.example.movieapp.data.repo;

import com.example.movieapp.common.MovieMapper;
import com.example.movieapp.data.database.MovieEntity;
import com.example.movieapp.data.model.details.basicDetails.CastResponse;
import com.example.movieapp.data.model.details.reviews.MovieDetailsReviewApiResponse;
import com.example.movieapp.data.model.home.MovieApiResponse;
import com.example.movieapp.data.networkServices.MovieApiService;
import com.example.movieapp.domain.models.CastUi;
import com.example.movieapp.domain.models.DetailsBasicUi;
import com.example.movieapp.domain.models.MovieUi;
import com.example.movieapp.domain.models.ReviewsUi;
import com.example.movieapp.domain.repoInterfaces.MovieDBRepository;
import com.example.movieapp.domain.repoInterfaces.MovieDetailsRepository;

import java.util.List;
import java.util.stream.Collectors;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class MovieDetailsRepositoryImpl implements MovieDetailsRepository {

    private MovieApiService apiService;
    MovieDBRepository movieDBRepository;

    public MovieDetailsRepositoryImpl(MovieApiService apiService,MovieDBRepository movieDBRepository) {
        this.apiService = apiService;
        this.movieDBRepository = movieDBRepository;
    }

    @Override
    public Single<DetailsBasicUi> getMovieById(Integer movie_id) {
        return Single.zip(
                apiService.getMovieById(movie_id)
                .map(MovieMapper::mapToUiModel),

                movieDBRepository.getMovieById(movie_id)
                        .map(MovieEntity::isFavorite)
                        .defaultIfEmpty(false),

                (details,isFavorite) -> {

                    details.setFavorite(isFavorite);
                    return details;
                }
        );

    }


    @Override
    public Single<List<ReviewsUi>> getReviewsById(Integer movie_id) {
        return apiService.getReviewsById(movie_id)
                .map(MovieDetailsReviewApiResponse::getResults)
                .map(movies ->
                    movies.stream()
                            .map(MovieMapper::mapToUiModel)
                            .collect(Collectors.toList())
                );
    }

    @Override
    public Single<List<CastUi>> getCreditsById (Integer movie_id){
        return apiService.getCreditsById(movie_id)
                .map(CastResponse::getCredits)
                .map(movies ->
                        movies.stream()
                                .map(MovieMapper::mapToUiModel)
                                .collect(Collectors.toList())
                );
    }

    @Override
    public Single<List<MovieUi>> getSimilarMoviesById(Integer movieId) {
        return apiService.getSimilarMovies(movieId)
                .map(MovieApiResponse::getResults)
                .map(movies ->
                        movies.stream()
                                .map(MovieMapper::mapToUiMovie)
                                .collect(java.util.stream.Collectors.toList())
                );
    }


}
