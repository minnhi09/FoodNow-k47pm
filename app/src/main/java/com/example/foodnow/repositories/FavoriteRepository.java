package com.example.foodnow.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodnow.models.Favorite;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FavoriteRepository {

    private final FirebaseFirestore db;

    public FavoriteRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /** Lấy danh sách yêu thích của user hiện tại */
    public LiveData<List<Favorite>> getFavorites() {
        MutableLiveData<List<Favorite>> liveData = new MutableLiveData<>();
        String userId = getCurrentUserId();

        db.collection("Favorites")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    List<Favorite> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Favorite fav = doc.toObject(Favorite.class);
                        fav.setId(doc.getId());
                        list.add(fav);
                    }
                    liveData.setValue(list);
                });

        return liveData;
    }

    /** Thêm yêu thích */
    public Task<Void> addFavorite(Favorite favorite) {
        return db.collection("Favorites").document().set(favorite);
    }

    /** Xóa yêu thích theo id */
    public Task<Void> removeFavorite(String favoriteId) {
        return db.collection("Favorites").document(favoriteId).delete();
    }

    private String getCurrentUserId() {
        return FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
    }
}
