package com.example.movieapp.data.networkServices;

import com.example.movieapp.data.model.details.basicDetails.CastRemote;
import com.example.movieapp.data.model.details.basicDetails.CastResponse;
import com.example.movieapp.data.model.details.basicDetails.MovieDetailsRemote;
import com.example.movieapp.data.model.details.reviews.MovieDetailsReviewApiResponse;
import com.example.movieapp.data.model.home.MovieApiResponse;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface MovieApiService {

    @GET("/3/movie/popular")
    Single<MovieApiResponse> getPopularMovies();

    @GET("/3/movie/{movie_id}")
    Single<MovieDetailsRemote> getMovieById(@Path("movie_id") Integer movie_id);
    @GET("/3/movie/{movie_id}/credits")
    Single<CastResponse> getCreditsById(@Path("movie_id") Integer movie_id);

    @GET("/3/movie/{movie_id}/reviews")
    Single<MovieDetailsReviewApiResponse> getReviewsById(@Path("movie_id") Integer movie_id);

    @GET("/3/movie/{movie_id}/similar")
    Single<MovieApiResponse> getSimilarMovies(@Path("movie_id") Integer movie_id);
}

