package com.example.movieapp.ui.homeScreen;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.movieapp.common.NetworkUtils;
import com.example.movieapp.domain.models.MovieUi;
import com.example.movieapp.domain.usecase.GetMoviesUseCase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class MovieViewModel extends ViewModel {

    private final GetMoviesUseCase getMoviesUseCase;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MediatorLiveData<List<MovieUi>> _movies = new MediatorLiveData<>();
    public final LiveData<List<MovieUi>> movies = _movies;

    private LiveData<List<MovieUi>> currentSource;
    private final LiveData<List<MovieUi>> allMoviesSource;
    private final LiveData<List<MovieUi>> favoritesOnlySource;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    @Inject
    public MovieViewModel(GetMoviesUseCase getMoviesUseCase) {
        this.getMoviesUseCase = getMoviesUseCase;
        this.allMoviesSource = getMoviesUseCase.execute();
        this.favoritesOnlySource = getMoviesUseCase.executeFavoritesOnly();
    }

    public void loadMovies(Context context) {
        if (NetworkUtils.isOnline(context)) {
            switchSource(allMoviesSource);
            refresh(context);
        } else {
            switchSource(favoritesOnlySource);
            _isLoading.setValue(false);
            _error.setValue("Offline mode: showing favorite movies only");
        }
    }

    private void switchSource(LiveData<List<MovieUi>> newSource) {
        if (currentSource != null) {
            _movies.removeSource(currentSource);
        }

        currentSource = newSource;
        _movies.addSource(currentSource, _movies::setValue);
    }

    public void refresh(Context context) {
        if (!NetworkUtils.isOnline(context)) {
            switchSource(favoritesOnlySource);
            _isLoading.setValue(false);
            _error.setValue("Offline mode: showing favorite movies only");
            return;
        }

        switchSource(allMoviesSource);
        _isLoading.setValue(true);

        disposables.add(
                getMoviesUseCase.refresh()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> _isLoading.setValue(false),
                                error -> {
                                    _isLoading.setValue(false);
                                    _error.setValue("Failed to load movies: " + error.getMessage());
                                }
                        )
        );
    }

    public void toggleFavorite(MovieUi movie) {
        disposables.add(
                getMoviesUseCase.toggleFavorite(movie.getId())
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                () -> { },
                                err -> _error.postValue("Toggle failed: " + err.getMessage())
                        )
        );
    }

    @Override
    protected void onCleared() {
        disposables.clear();
        super.onCleared();
    }

    public static String formatDate(String inputDate) {
        if (inputDate == null || inputDate.isEmpty()) {
            return "Unknown";
        }

        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            SimpleDateFormat output = new SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH);

            Date date = input.parse(inputDate);
            return output.format(date);

        } catch (Exception e) {
            e.printStackTrace();
            return inputDate;
        }
    }
}