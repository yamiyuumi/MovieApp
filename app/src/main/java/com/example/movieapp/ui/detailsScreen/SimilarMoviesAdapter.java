package com.example.movieapp.ui.detailsScreen;

import static com.example.movieapp.ui.homeScreen.MovieViewModel.formatDate;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.movieapp.R;
import com.example.movieapp.databinding.ItemSimilarMovieBinding;
import com.example.movieapp.domain.models.MovieUi;

public class SimilarMoviesAdapter extends ListAdapter<MovieUi, SimilarMoviesAdapter.SimilarMovieViewHolder> {

    private final OnItemClickListener itemClickListener;

    public SimilarMoviesAdapter(OnItemClickListener itemClickListener) {
        super(new DiffCallback());
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public SimilarMovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSimilarMovieBinding binding = ItemSimilarMovieBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new SimilarMovieViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SimilarMovieViewHolder holder, int position) {
        MovieUi movie = getItem(position);
        holder.bind(movie);
    }

    class SimilarMovieViewHolder extends RecyclerView.ViewHolder {

        private final ItemSimilarMovieBinding binding;

        SimilarMovieViewHolder(ItemSimilarMovieBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(MovieUi movie) {
            Glide.with(binding.similarMoviePoster.getContext())
                    .load("https://image.tmdb.org/t/p/w500" + movie.getPosterPath())
                    .apply(new RequestOptions().placeholder(R.drawable.loading))
                    .into(binding.similarMoviePoster);

            binding.similarMovieTitle.setText(movie.getTitle());
            binding.similarMovieDate.setText(formatDate(movie.getReleaseDate()));

            binding.getRoot().setOnClickListener(v -> {
                itemClickListener.onItemClick(movie.getId());
            });
//            binding.getRoot().setOnClickListener(null);
//            binding.getRoot().setClickable(false);
        }
    }

    static class DiffCallback extends DiffUtil.ItemCallback<MovieUi> {
        @Override
        public boolean areItemsTheSame(@NonNull MovieUi oldItem, @NonNull MovieUi newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull MovieUi oldItem, @NonNull MovieUi newItem) {
            return oldItem.getId() == newItem.getId()
                    && oldItem.isFavorite() == newItem.isFavorite()
                    && oldItem.getTitle().equals(newItem.getTitle())
                    && oldItem.getPosterPath().equals(newItem.getPosterPath())
                    && oldItem.getReleaseDate().equals(newItem.getReleaseDate());
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int movieId);
    }
}