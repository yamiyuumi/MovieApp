package com.example.movieapp.domain.repoInterfaces;

import com.example.movieapp.domain.models.CastUi;
import com.example.movieapp.domain.models.DetailsBasicUi;
import com.example.movieapp.domain.models.MovieUi;
import com.example.movieapp.domain.models.ReviewsUi;

import java.util.List;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface MovieDetailsRepository {

    Single<DetailsBasicUi> getMovieById(Integer movie_id);
    Single<List<ReviewsUi>> getReviewsById(Integer movie_id);

    Single<List<CastUi>> getCreditsById(Integer movie_id);
    Single<List<MovieUi>> getSimilarMoviesById(Integer movieId);

}
