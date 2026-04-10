package com.example.movieapp.domain.models;


import com.example.movieapp.data.model.details.basicDetails.CastRemote;

import java.util.List;

public class MovieDetailsWithReviewsUi {

    private final DetailsBasicUi movieDetails;
    private final List<ReviewsUi> reviews;

    private final List<CastUi> credits;
    private List<MovieUi> similarMovies;


    public MovieDetailsWithReviewsUi(DetailsBasicUi movieDetails,List<MovieUi> similarMovies, List<ReviewsUi> reviews,List<CastUi> credits) {
        this.movieDetails = movieDetails;
        this.similarMovies = similarMovies;
        this.reviews = reviews;
        this.credits = credits;
    }

    public List<CastUi> getCredits() {
        return credits;
    }

    public DetailsBasicUi getMovieDetails() {
        return movieDetails;
    }

    public List<MovieUi> getSimilarMovies() {
        return similarMovies;
    }

    public List<ReviewsUi> getReviews() {
        return reviews;
    }
//    public ReviewsUi getReviews() {
//        if (reviews != null && !reviews.isEmpty()) {
//            return reviews.get(0);
//        }
//        return null;
//    }
}
