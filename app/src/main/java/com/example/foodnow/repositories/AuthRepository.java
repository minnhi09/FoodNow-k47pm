package com.example.foodnow.repositories;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

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

    /** Đăng ký tài khoản mới + lưu thông tin user vào Firestore */
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
                        userData.put("createdAt", com.google.firebase.Timestamp.now());

                        db.collection("Users")
                                .document(firebaseUser.getUid())
                                .set(userData);
                    }
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
}
