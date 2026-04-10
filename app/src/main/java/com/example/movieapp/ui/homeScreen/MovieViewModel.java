package com.example.movieapp.ui.homeScreen;

import android.text.format.DateUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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

    /**
     * CompositeDisposable lives here — cleared in onCleared().
     * This is the correct owner: the ViewModel knows its own lifecycle.
     */
    private final CompositeDisposable disposables = new CompositeDisposable();

    /** LiveData backed by Room — auto-updates when DB changes. */
    public final LiveData<List<MovieUi>> movies;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    @Inject
    public MovieViewModel(GetMoviesUseCase getMoviesUseCase) {
        this.getMoviesUseCase = getMoviesUseCase;
        this.movies = getMoviesUseCase.execute();
        refresh(); // trigger initial network load
    }

    /**
     * Triggers a network refresh. The Completable from the use case
     * is subscribed here; disposable is stored and cleared in onCleared().
     */
    public void refresh() {
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

    /**
     * Atomic toggle by movie ID — delegates to SQL-level toggle.
     * Disposable is added to the same CompositeDisposable.
     */
    public void toggleFavorite(MovieUi movie) {
        disposables.add(
                getMoviesUseCase.toggleFavorite(movie.getId())
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                () -> { /* success — LiveData re-emits automatically */ },
                                err -> _error.setValue("Toggle failed: " + err.getMessage())
                        )
        );
    }

    @Override
    protected void onCleared() {
        disposables.clear(); // safe lifecycle cleanup
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
