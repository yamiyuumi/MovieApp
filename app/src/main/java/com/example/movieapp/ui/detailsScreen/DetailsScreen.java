package com.example.movieapp.ui.detailsScreen;

import static com.example.movieapp.ui.homeScreen.MovieViewModel.formatDate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.movieapp.R;
//import com.google.android.material.R;
import com.example.movieapp.databinding.DetailsLayoutBinding;
import com.example.movieapp.domain.models.CastUi;
import com.example.movieapp.domain.models.GenreUi;
import com.example.movieapp.domain.models.MovieDetailsWithReviewsUi;
import com.example.movieapp.domain.models.MovieUi;
import com.example.movieapp.domain.models.ReviewsUi;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DetailsScreen extends Fragment {

    private DetailsLayoutBinding binding;
    private DetailsViewModel detailsViewModel;

    private SimilarMoviesAdapter similarMoviesAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DetailsLayoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        detailsViewModel = new ViewModelProvider(this).get(DetailsViewModel.class);


        similarMoviesAdapter = new SimilarMoviesAdapter((movieId, isFavorite) -> {
            Bundle bundle = new Bundle();
            bundle.putInt("MOVIE_ID", movieId);

            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_movieListFragment_to_detailsScreen, bundle);
        });

        binding.similarMoviesRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        binding.similarMoviesRecyclerView.setAdapter(similarMoviesAdapter);


        if (getArguments() != null) {
            int movieId = getArguments().getInt("MOVIE_ID", -1);
            if (movieId != -1) {
                detailsViewModel.fetchMovieDetailsWithReviews(movieId);
            }
        }

        observeViewModel();
        binding.backButton.setOnClickListener(view1 -> {
            Navigation.findNavController(binding.getRoot()).navigateUp();
        });
    }

    private void observeViewModel() {
        detailsViewModel.movieDetailsLiveData.observe(getViewLifecycleOwner(),
                this::updateUI
        );

        detailsViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading ->
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE)
        );

        detailsViewModel.error.observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Snackbar.make(binding.getRoot(), errorMsg, Snackbar.LENGTH_LONG).show();
            }
        });


    }

    private void updateUI(MovieDetailsWithReviewsUi data) {
        if (data == null) return;
        // TODO: bind data.getMovieDetails() and data.getReviews() to XML views

        if (data == null) return;
        binding.movieTitle.setText(data.getMovieDetails().getTitle());

        binding.movieDescription.setText(data.getMovieDetails().getOverview());

        List<String> genreNames = new ArrayList<>();
        for (GenreUi genre : data.getMovieDetails().getGenres()) {
            genreNames.add(genre.getName());
        }

        List<String> castNames = new ArrayList<>();
        int max = Math.min(data.getCredits().size(), 10);

        for (int i = 0; i < max; i++) {
            castNames.add(data.getCredits().get(i).getName());
        }

        binding.movieGenre.setText(TextUtils.join(", ", genreNames));


        binding.movieCast.setText(TextUtils.join(", ", castNames)); ///////OXI GENRE NAMES

        binding.releaseDate.setText(formatDate(data.getMovieDetails().getReleaseDate()));

        binding.movieRating.setRating((float) (data.getMovieDetails().getVoteAverage() / 2));

        int runtime = data.getMovieDetails().getRuntime();
        int hours = runtime / 60;
        int minutes = runtime % 60;
        binding.movieRuntime.setText(hours + "h " + minutes + "m");

        Glide.with(requireContext())
                .load("https://image.tmdb.org/t/p/w500" + data.getMovieDetails().getPosterPath())
                .placeholder(R.drawable.loading)
                .into(binding.moviePoster);
        renderReviews(data.getReviews());

        List<MovieUi> similarMovies = data.getSimilarMovies();
        if (similarMovies != null){
            similarMoviesAdapter.submitList(
                    similarMovies.subList(0,Math.min(similarMovies.size(),6))
            );
        }
//        similarMoviesAdapter.submitList(data.getSimilarMovies());
        binding.favoriteIcon.setOnClickListener(view -> {
            int movieId = data.getMovieDetails().getId();
            detailsViewModel.toggleFavorite(movieId);
        });

        String homepage = data.getMovieDetails().getHomepage();
        if (homepage == null || homepage.isEmpty()){
            binding.shareIcon.setVisibility(View.GONE);
        }else{
            binding.shareIcon.setVisibility(View.VISIBLE);

            binding.shareIcon.setOnClickListener(view -> {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, homepage);

                startActivity(Intent.createChooser(shareIntent, " Share movie"));
            });
        }

    }
    private void renderReviews(java.util.List<com.example.movieapp.domain.models.ReviewsUi> reviews) {
        binding.reviewsContainer.removeAllViews();

        if (reviews == null || reviews.isEmpty()) {
            TextView emptyView = new TextView(requireContext());
            emptyView.setText("No reviews available");
            emptyView.setTextSize(14f);
            emptyView.setTextColor(android.graphics.Color.parseColor("#FFD54F"));
            binding.reviewsContainer.addView(emptyView);
            return;
        }

        int max = Math.min(reviews.size(), 3);

        for (int i = 0; i < max; i++) {
            com.example.movieapp.domain.models.ReviewsUi review = reviews.get(i);

            LinearLayout reviewBlock = new LinearLayout(requireContext());
            reviewBlock.setOrientation(LinearLayout.VERTICAL);

            LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            blockParams.bottomMargin = dpToPx(16);
            reviewBlock.setLayoutParams(blockParams);

            TextView authorView = new TextView(requireContext());
            authorView.setText(review.getAuthor());
            authorView.setTextSize(15f);
            authorView.setTypeface(null, android.graphics.Typeface.BOLD);
            authorView.setTextColor(android.graphics.Color.parseColor("#FFD54F"));

            TextView contentView = new TextView(requireContext());
            String content = review.getContent();

            if (content != null && content.length() > 300) {
                content = content.substring(0, 300) + "...";
            }

            contentView.setText(content);
            contentView.setTextSize(14f);
            contentView.setTextColor(resolveThemeColor(com.google.android.material.R.attr.colorOnBackground));

            LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            contentParams.topMargin = dpToPx(4);
            contentView.setLayoutParams(contentParams);

            reviewBlock.addView(authorView);
            reviewBlock.addView(contentView);

            binding.reviewsContainer.addView(reviewBlock);
        }
    }
    private int dpToPx(int dp) {
        return Math.round(dp * requireContext().getResources().getDisplayMetrics().density);
    }
    private int resolveThemeColor(int attrRes) {
        android.util.TypedValue typedValue = new android.util.TypedValue();
        requireContext().getTheme().resolveAttribute(attrRes, typedValue, true);
        return typedValue.data;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
