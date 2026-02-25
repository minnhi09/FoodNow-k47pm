package com.example.foodnow.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodnow.models.Favorite;
import com.example.foodnow.repositories.FavoriteRepository;

import java.util.List;

public class FavoritesViewModel extends ViewModel {

    private final FavoriteRepository favoriteRepository;
    private final LiveData<List<Favorite>> favorites;

    public FavoritesViewModel() {
        favoriteRepository = new FavoriteRepository();
        favorites = favoriteRepository.getFavorites();
    }

    public LiveData<List<Favorite>> getFavorites() { return favorites; }

    public void removeFavorite(String favoriteId) {
        favoriteRepository.removeFavorite(favoriteId);
    }
}
