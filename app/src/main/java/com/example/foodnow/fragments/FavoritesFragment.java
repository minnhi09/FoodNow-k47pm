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
    private FavoriteAdapter favoriteAdapter;
    private List<Favorite> favoriteList = new ArrayList<>();
    private TextView tvEmpty;
    private FavoritesViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        rvFavorites = view.findViewById(R.id.rv_favorites);
        tvEmpty     = view.findViewById(R.id.tv_favorites_empty);

        viewModel = new ViewModelProvider(this).get(FavoritesViewModel.class);

        favoriteAdapter = new FavoriteAdapter(getContext(), favoriteList, fav -> {
            viewModel.removeFavorite(fav.getId());
        });
        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFavorites.setAdapter(favoriteAdapter);

        viewModel.getFavorites().observe(getViewLifecycleOwner(), favorites -> {
            favoriteList.clear();
            favoriteList.addAll(favorites);
            favoriteAdapter.notifyDataSetChanged();

            rvFavorites.setVisibility(favorites.isEmpty() ? View.GONE : View.VISIBLE);
            tvEmpty.setVisibility(favorites.isEmpty() ? View.VISIBLE : View.GONE);
        });

        return view;
    }
}
