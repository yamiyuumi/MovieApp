package com.example.movieapp.ui.detailsScreen;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.movieapp.domain.models.MovieDetailsWithReviewsUi;
import com.example.movieapp.domain.models.MovieUi;
import com.example.movieapp.domain.usecase.DetailsUseCase;
import com.example.movieapp.domain.usecase.GetMoviesUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class DetailsViewModel extends ViewModel {

    private final DetailsUseCase detailsUseCase;
    private final GetMoviesUseCase getMoviesUseCase;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<MovieDetailsWithReviewsUi> _movieDetails = new MutableLiveData<>();
    public final LiveData<MovieDetailsWithReviewsUi> movieDetailsLiveData = _movieDetails;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    @Inject
    public DetailsViewModel(DetailsUseCase detailsUseCase, GetMoviesUseCase getMoviesUseCase) {
        this.detailsUseCase = detailsUseCase;
        this.getMoviesUseCase = getMoviesUseCase;
    }

    public void fetchMovieDetailsWithReviews(int movieId) {
        _isLoading.setValue(true);
        disposables.add(
                detailsUseCase.execute(movieId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                details -> {
                                    _isLoading.setValue(false);
                                    _movieDetails.setValue(details);
                                },
                                throwable -> {
                                    _isLoading.setValue(false);
                                    _error.setValue("Error loading details: " + throwable.getMessage());
                                    Log.e("DetailsViewModel", "fetchMovieDetailsWithReviews", throwable);
                                }
                        )
        );
    }
    public void toggleFavorite(int movieId) {
        disposables.add(
                getMoviesUseCase.toggleFavorite(movieId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> { /* success — LiveData re-emits automatically */ },
                                err -> _error.setValue("Toggle failed: " + err.getMessage())
                        )
        );
    }
    @Override
    protected void onCleared() {
        disposables.clear();
        super.onCleared();
    }
}
