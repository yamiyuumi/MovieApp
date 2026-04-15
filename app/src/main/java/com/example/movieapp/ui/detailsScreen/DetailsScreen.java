package com.example.movieapp.ui.detailsScreen;

import static com.example.movieapp.ui.homeScreen.MovieViewModel.formatDate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.movieapp.R;
//import com.google.android.material.R;
import com.example.movieapp.databinding.DetailsLayoutBinding;
import com.example.movieapp.domain.models.GenreUi;
import com.example.movieapp.domain.models.MovieDetailsWithReviewsUi;
import com.example.movieapp.domain.models.MovieUi;
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

        setUpSimilarRecycler();
        observeViewModel();
        setUpBackButton();
        loadMovieFromArguments();

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
    private void setUpBackButton(){
        binding.backButton.setOnClickListener(view1 -> {
            Navigation.findNavController(binding.getRoot()).navigateUp();
        });
    }
    private void setUpSimilarRecycler(){
        similarMoviesAdapter = new SimilarMoviesAdapter((movieId) -> {
            Bundle bundle = new Bundle();
            bundle.putInt("MOVIE_ID", movieId);

//            Navigation.findNavController(binding.getRoot())
//                    .navigate(R.id.action_movieListFragment_to_detailsScreen, bundle);
//        });
            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_detalsScreen_self, bundle);
        });

        binding.similarMoviesRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        binding.similarMoviesRecyclerView.setAdapter(similarMoviesAdapter);
    }
    private void loadMovieFromArguments(){
        if (!com.example.movieapp.common.NetworkUtils.isOnline(requireContext())){
            Snackbar.make(binding.getRoot(),
                            "Details are unavailable offline",
                            Snackbar.LENGTH_SHORT)
                    .show();
            Navigation.findNavController(binding.getRoot()).navigateUp();
            return;

        }
        if (getArguments() != null) {
            int movieId = getArguments().getInt("MOVIE_ID", -1);
            if (movieId != -1) {
                detailsViewModel.fetchMovieDetailsWithReviews(movieId);
            }
        }
    }
    private void updateUI(MovieDetailsWithReviewsUi data) {
        if (data == null || data.getMovieDetails() == null) return;
        // TODO: bind data.getMovieDetails() and data.getReviews() to XML views
        bindBasicInfo(data);
        bindPoster(data);
        renderReviews(data.getReviews());
        bindSimilarMovies(data);
        bindShare(data);
        bindFavorite(data);
    }
    public void bindFavorite(MovieDetailsWithReviewsUi data){
        updateFavoriteIcon(data.getMovieDetails().isFavorite());

        binding.favoriteIcon.setOnClickListener(v ->{
            int movieId = data.getMovieDetails().getId();
            detailsViewModel.toggleFavorite(movieId);

            boolean newState = !data.getMovieDetails().isFavorite();
            data.getMovieDetails().setFavorite(newState);

            updateFavoriteIcon(newState);
        });
    }
    public void updateFavoriteIcon(boolean isFavorite){
        binding.favoriteIcon.setImageResource(
                isFavorite
                        ? R.drawable.ic_favorite_selected
                        : R.drawable.ic_favorite_unselect

        );
    }

    public void bindShare(MovieDetailsWithReviewsUi data){
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
    private void bindSimilarMovies(MovieDetailsWithReviewsUi data){
        List<MovieUi> similarMovies = data.getSimilarMovies();

        if (similarMovies == null || similarMovies.isEmpty()){
            binding.similarMovies.setVisibility(View.GONE);
            binding.similarMoviesRecyclerView.setVisibility(View.GONE);
            return;
        }
        binding.similarMovies.setVisibility(View.VISIBLE);
        binding.similarMoviesRecyclerView.setVisibility(View.VISIBLE);
        similarMoviesAdapter.submitList(
                similarMovies.subList(0,Math.min(similarMovies.size(),6))
        );
    }
    private void bindBasicInfo(MovieDetailsWithReviewsUi data){
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

//        binding.movieRating.setRating((float) (data.getMovieDetails().getVoteAverage() / 2));

        bindCustomRating(data.getMovieDetails().getVoteAverage());

        int runtime = data.getMovieDetails().getRuntime();
        int hours = runtime / 60;
        int minutes = runtime % 60;
        binding.movieRuntime.setText(hours + "h " + minutes + "m");


    }
    private void bindPoster(MovieDetailsWithReviewsUi data){
        Glide.with(requireContext())
                .load("https://image.tmdb.org/t/p/w500" + data.getMovieDetails().getPosterPath())
                .placeholder(R.drawable.loading)
                .into(binding.moviePoster);
    }
    private void renderReviews(java.util.List<com.example.movieapp.domain.models.ReviewsUi> reviews) {
        binding.reviewsContainer.removeAllViews();

        if (reviews == null || reviews.isEmpty()) {
            TextView emptyView = new TextView(requireContext());
            emptyView.setText("No reviews available");
            emptyView.setTextSize(13f);
            emptyView.setTextColor(ContextCompat.getColor(requireContext(),R.color.orangish));
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
            authorView.setTextSize(12f);
            authorView.setTypeface(null, android.graphics.Typeface.BOLD);
            authorView.setTextColor(ContextCompat.getColor(requireContext(),R.color.orangish));

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

    private void bindCustomRating(double voteAverage){
        int rating = (int) Math.round(voteAverage/2.0);
        ImageView[] stars = {
                binding.star1,
                binding.star2,
                binding.star3,
                binding.star4,
                binding.star5
        };
        for (int i=0; i<5; i++){
            if(i < rating){
                stars[i].setImageResource(R.drawable.star_full);
            }else{
                stars[i].setImageResource(R.drawable.star_empty);

            }
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
