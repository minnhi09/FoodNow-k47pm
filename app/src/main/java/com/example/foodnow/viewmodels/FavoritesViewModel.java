package com.example.foodnow.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodnow.models.Favorite;
import com.example.foodnow.repositories.FavoriteRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class FavoritesViewModel extends ViewModel {

    private final FavoriteRepository favoriteRepository;
    private final LiveData<List<Favorite>> favorites;

    public FavoritesViewModel() {
        favoriteRepository = new FavoriteRepository();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user != null ? user.getUid() : null;

        if (userId != null && !userId.trim().isEmpty()) {
            favorites = favoriteRepository.getFavorites(userId);
        } else {
            MutableLiveData<List<Favorite>> emptyLiveData = new MutableLiveData<>();
            emptyLiveData.setValue(new ArrayList<>());
            favorites = emptyLiveData;
        }
    }

    public LiveData<List<Favorite>> getFavorites() {
        return favorites;
    }

    public void removeFavorite(String favoriteId) {
        favoriteRepository.removeFavorite(favoriteId);
    }
}
