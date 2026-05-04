package com.example.foodnow.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodnow.models.Favorite;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FavoriteRepository {

    private final FirebaseFirestore db;

    public FavoriteRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public LiveData<List<Favorite>> getFavorites(String userId) {
        MutableLiveData<List<Favorite>> liveData = new MutableLiveData<>();

        db.collection("Favorites")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) {
                        return;
                    }

                    List<Favorite> favorites = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Favorite favorite = doc.toObject(Favorite.class);
                        favorite.setId(doc.getId());
                        favorites.add(favorite);
                    }
                    liveData.setValue(favorites);
                });

        return liveData;
    }

    public void checkFavorite(String userId, String itemId, String type, FavoriteCheckCallback callback) {
        db.collection("Favorites")
                .whereEqualTo("userId", userId)
                .whereEqualTo("itemId", itemId)
                .whereEqualTo("type", type)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (snapshots != null && !snapshots.isEmpty()) {
                        String id = snapshots.getDocuments().get(0).getId();
                        callback.onResult(true, id);
                    } else {
                        callback.onResult(false, null);
                    }
                });
    }

    public Task<DocumentReference> addFavorite(Favorite favorite) {
        return db.collection("Favorites").add(favorite);
    }

    public Task<Void> removeFavorite(String favoriteId) {
        return db.collection("Favorites").document(favoriteId).delete();
    }

    public interface FavoriteCheckCallback {
        void onResult(boolean isFavorited, String id);
    }
}
