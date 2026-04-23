package com.example.foodnow.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodnow.models.Favorite;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/** Truy cập Firestore collection "Favorites". */
public class FavoriteRepository {

    private static final String COLLECTION = "Favorites";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Lắng nghe real-time danh sách quán yêu thích của user.
     */
    public LiveData<List<Favorite>> getStoreFavorites(String userId) {
        MutableLiveData<List<Favorite>> liveData = new MutableLiveData<>();
        if (userId == null || userId.isEmpty()) {
            liveData.setValue(new ArrayList<>());
            return liveData;
        }
        db.collection(COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", "store")
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

    /**
     * Kiểm tra user đã yêu thích item chưa.
     * LiveData trả về favoriteId nếu đã yêu thích, null nếu chưa.
     */
    public LiveData<String> checkIsFavorite(String userId, String type, String itemId) {
        MutableLiveData<String> liveData = new MutableLiveData<>(null);
        if (userId == null || userId.isEmpty() || itemId == null || itemId.isEmpty()) {
            return liveData;
        }
        db.collection(COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", type)
                .whereEqualTo("itemId", itemId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) {
                        liveData.setValue(null);
                        return;
                    }
                    if (snapshots.isEmpty()) {
                        liveData.setValue(null);
                    } else {
                        DocumentSnapshot doc = snapshots.getDocuments().get(0);
                        liveData.setValue(doc.getId());
                    }
                });
        return liveData;
    }

    /** Thêm vào yêu thích. */
    public Task<Void> addFavorite(Favorite favorite) {
        return db.collection(COLLECTION)
                .document()
                .set(favorite)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    return task;
                });
    }

    /** Xóa khỏi yêu thích theo documentId. */
    public Task<Void> removeFavorite(String favoriteId) {
        return db.collection(COLLECTION)
                .document(favoriteId)
                .delete();
    }
}
