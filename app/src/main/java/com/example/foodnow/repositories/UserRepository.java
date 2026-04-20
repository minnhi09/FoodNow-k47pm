package com.example.foodnow.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodnow.models.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Map;

public class UserRepository {

    private final FirebaseFirestore db;

    public UserRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /** Lấy thông tin user hiện tại từ Firestore */
    public LiveData<User> getCurrentUser() {
        MutableLiveData<User> liveData = new MutableLiveData<>();
        String uid = getCurrentUserId();
        if (uid.isEmpty()) return liveData;

        db.collection("Users").document(uid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) return;

                    User user = snapshot.toObject(User.class);
                    if (user != null) {
                        user.setId(snapshot.getId());
                        liveData.setValue(user);
                    }
                });

        return liveData;
    }

    /** Cập nhật thông tin user */
    public Task<Void> updateUser(Map<String, Object> updates) {
        String uid = getCurrentUserId();
        if (uid.isEmpty()) {
            return Tasks.forException(new IllegalStateException("Chưa đăng nhập nên không thể cập nhật user"));
        }
        return db.collection("Users").document(uid).set(updates, SetOptions.merge());
    }

    private String getCurrentUserId() {
        return FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
    }
}
