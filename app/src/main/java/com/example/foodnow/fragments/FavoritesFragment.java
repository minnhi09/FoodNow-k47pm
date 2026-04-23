package com.example.foodnow.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.R;
import com.example.foodnow.adapters.FavoriteAdapter;
import com.example.foodnow.models.Favorite;
import com.example.foodnow.viewmodels.FavoritesViewModel;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private RecyclerView rvFavorites;
    private TextView tvFavoritesEmpty;
    private FavoriteAdapter adapter;
    private final List<Favorite> favoriteList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvFavorites = view.findViewById(R.id.rv_favorites);
        tvFavoritesEmpty = view.findViewById(R.id.tv_favorites_empty);

        FavoritesViewModel viewModel = new ViewModelProvider(this).get(FavoritesViewModel.class);

        adapter = new FavoriteAdapter(requireContext(), favoriteList, fav -> {
            if (fav != null && fav.getId() != null) {
                viewModel.removeFavorite(fav.getId());
            }
        });

        rvFavorites.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFavorites.setAdapter(adapter);

        viewModel.getFavorites().observe(getViewLifecycleOwner(), favorites -> {
            favoriteList.clear();
            if (favorites != null) {
                favoriteList.addAll(favorites);
            }
            adapter.notifyDataSetChanged();

            boolean isEmpty = favoriteList.isEmpty();
            tvFavoritesEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            rvFavorites.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });
    }
}
