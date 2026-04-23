package com.example.foodnow.repositories;

import com.example.foodnow.models.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class AuthRepository {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public AuthRepository() {
        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();
    }

    /** Đăng nhập bằng email + mật khẩu */
    public Task<AuthResult> login(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password);
    }

    /** Đăng ký tài khoản khách hàng + lưu thông tin user vào Firestore */
    public Task<AuthResult> register(String email, String password, String name, String phone) {
        return auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser firebaseUser = result.getUser();
                    if (firebaseUser != null) {
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("email", email);
                        userData.put("name", name);
                        userData.put("phone", phone);
                        userData.put("address", "");
                        userData.put("imageUrl", "");
                        userData.put("role", "customer");
                        userData.put("createdAt", Timestamp.now());

                        db.collection("Users")
                                .document(firebaseUser.getUid())
                                .set(userData);
                    }
                });
    }

    /**
     * Đăng ký tài khoản chủ cửa hàng.
     * Dùng WriteBatch để ghi đồng thời Users/{uid} và Stores/{newStoreId}.
     * Trả về Task<String> chứa storeId mới.
     */
    public Task<String> registerStoreOwner(String email, String password,
                                           String name, String phone,
                                           String storeName, String storeAddress,
                                           String storePhone, String deliveryTime,
                                           long deliveryFee) {
        return auth.createUserWithEmailAndPassword(email, password)
                .continueWithTask(authTask -> {
                    if (!authTask.isSuccessful() || authTask.getResult() == null) {
                        throw authTask.getException() != null
                                ? authTask.getException()
                                : new Exception("Đăng ký thất bại");
                    }

                    FirebaseUser firebaseUser = authTask.getResult().getUser();
                    if (firebaseUser == null) throw new Exception("Không thể tạo tài khoản");

                    String uid = firebaseUser.getUid();

                    // Tạo document reference trước để lấy storeId tự sinh
                    DocumentReference storeRef = db.collection("Stores").document();
                    String storeId = storeRef.getId();

                    WriteBatch batch = db.batch();

                    // Document Users/{uid}
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("email", email);
                    userData.put("name", name);
                    userData.put("phone", phone);
                    userData.put("address", "");
                    userData.put("imageUrl", "");
                    userData.put("role", "store_owner");
                    userData.put("storeId", storeId);
                    userData.put("createdAt", Timestamp.now());
                    batch.set(db.collection("Users").document(uid), userData);

                    // Document Stores/{storeId}
                    Map<String, Object> storeData = new HashMap<>();
                    storeData.put("name", storeName);
                    storeData.put("description", "");
                    storeData.put("address", storeAddress);
                    storeData.put("phone", storePhone);
                    storeData.put("imageUrl", "");
                    storeData.put("rating", 0.0);
                    storeData.put("deliveryTime", deliveryTime.isEmpty() ? "20 phút" : deliveryTime);
                    storeData.put("deliveryFee", deliveryFee);
                    storeData.put("isOpen", true);
                    storeData.put("storeOwnerId", uid);
                    storeData.put("categoryId", "");
                    batch.set(storeRef, storeData);

                    // Commit batch, rồi trả về storeId
                    return batch.commit().continueWith(commitTask -> {
                        if (!commitTask.isSuccessful()) {
                            throw commitTask.getException() != null
                                    ? commitTask.getException()
                                    : new Exception("Lỗi lưu dữ liệu");
                        }
                        return storeId;
                    });
                });
    }

    /** Lấy user hiện tại (null nếu chưa đăng nhập) */
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    /** Đăng xuất */
    public void logout() {
        auth.signOut();
    }

    /**
     * Lấy thông tin profile (bao gồm role + storeId) từ Firestore.
     * Dùng một lần (get) — không cần real-time listener.
     */
    public Task<User> getUserProfile(String uid) {
        return db.collection("Users")
                .document(uid)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        User user = task.getResult().toObject(User.class);
                        if (user != null) user.setId(uid);
                        return user;
                    }
                    return null;
                });
    }
}
