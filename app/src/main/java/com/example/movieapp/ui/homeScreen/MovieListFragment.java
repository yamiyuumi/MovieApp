package com.example.movieapp.ui.homeScreen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.movieapp.R;
import com.example.movieapp.common.NetworkUtils;
import com.example.movieapp.databinding.FragmentMovieBinding;
import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MovieListFragment extends Fragment {

    private MovieViewModel viewModel;
    private MovieListAdapter adapter;
    private FragmentMovieBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMovieBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override
    public void onResume(){
        super.onResume();

//        boolean isOnline = com.example.movieapp.common.NetworkUtils.isOnline(requireContext());
//        adapter.setDetailsEnabled(isOnline);

        viewModel.loadMovies(requireContext());
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new MovieListAdapter(
                movie -> viewModel.toggleFavorite(movie),
                (movieId, isfavorite) -> {
                    if (!NetworkUtils.isOnline(requireContext())){
                        Snackbar.make(binding.getRoot(),
                        "Details are unavailable offline",
                                Snackbar.LENGTH_SHORT)
                                .show();
                    return;
                    }
                    Bundle bundle = new Bundle();
                    bundle.putInt("MOVIE_ID", movieId);
                    Navigation.findNavController(binding.getRoot())
                            .navigate(R.id.action_movieListFragment_to_detailsScreen, bundle);
                }
        );

//        boolean isOnline = com.example.movieapp.common.NetworkUtils.isOnline(requireContext());

//        adapter.setDetailsEnabled(isOnline);

        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setItemAnimator(null);

        viewModel = new ViewModelProvider(this).get(MovieViewModel.class);
        viewModel.loadMovies(requireContext());
        binding.swipeRefreshLayout.setOnRefreshListener(() ->{
            viewModel.refresh(requireContext());
        });

        viewModel.movies.observe(getViewLifecycleOwner(), movies -> {
            adapter.submitList(movies);
            boolean isOffline = !NetworkUtils.isOnline(requireContext());

            if(isOffline &&(movies == null || movies.isEmpty())){
                binding.emptyView.setVisibility(View.VISIBLE);
                binding.recyclerView.setVisibility(View.GONE);
            } else {
                binding.emptyView.setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.VISIBLE);
            }
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.swipeRefreshLayout.setRefreshing(isLoading);
        });


        viewModel.error.observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Snackbar.make(binding.getRoot(), errorMsg, Snackbar.LENGTH_LONG)
                        .setAction("Retry", v -> viewModel.refresh(requireContext()))
                        .show();
                viewModel.clearError();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}