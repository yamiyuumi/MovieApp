package com.example.movieapp.domain.usecase;

import com.example.movieapp.domain.models.CastUi;
import com.example.movieapp.domain.models.DetailsBasicUi;
import com.example.movieapp.domain.models.MovieDetailsWithReviewsUi;
import com.example.movieapp.domain.models.MovieUi;
import com.example.movieapp.domain.models.ReviewsUi;
import com.example.movieapp.domain.repoInterfaces.MovieDetailsRepository;

import java.util.List;

import javax.inject.Inject;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class DetailsUseCase {

    private MovieDetailsRepository movieDetailsRepository;

    @Inject
    public DetailsUseCase(MovieDetailsRepository movieDetailsRepository) {
        this.movieDetailsRepository = movieDetailsRepository;
    }

    public Single<MovieDetailsWithReviewsUi> execute(Integer movieId) {
        Single<DetailsBasicUi> movieDetailsObservable = movieDetailsRepository.getMovieById(movieId);
        Single<List<ReviewsUi>> reviewsObservable = movieDetailsRepository.getReviewsById(movieId);
        Single<List<CastUi>> creditsObservable = movieDetailsRepository.getCreditsById(movieId);
        Single<List<MovieUi>> similarMoviesObservable = movieDetailsRepository.getSimilarMoviesById(movieId);


        // Waits for all, emits once
        return Single.zip(
                movieDetailsObservable,
                similarMoviesObservable,
                reviewsObservable,
                creditsObservable,
                MovieDetailsWithReviewsUi::new // (movie, reviews) -> new MovieDetailsWithReviewsUi(movie, reviews)
        );
    }

}
