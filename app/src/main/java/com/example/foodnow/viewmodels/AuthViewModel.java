package com.example.foodnow.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodnow.models.User;
import com.example.foodnow.repositories.AuthRepository;
import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;

    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    // Profile đầy đủ (có role + storeId) — dùng để route sau login
    private final MutableLiveData<User> userProfileLiveData = new MutableLiveData<>();

    public AuthViewModel() {
        authRepository = new AuthRepository();
        // Nếu đã đăng nhập trước đó → set user ngay
        if (authRepository.getCurrentUser() != null) {
            userLiveData.setValue(authRepository.getCurrentUser());
        }
    }

    public LiveData<FirebaseUser> getUserLiveData()  { return userLiveData; }
    public LiveData<String> getErrorLiveData()       { return errorLiveData; }
    public LiveData<Boolean> getLoadingLiveData()    { return loadingLiveData; }
    public LiveData<User> getUserProfileLiveData()   { return userProfileLiveData; }

    /** Đăng nhập */
    public void login(String email, String password) {
        loadingLiveData.setValue(true);
        authRepository.login(email, password)
                .addOnSuccessListener(result -> {
                    userLiveData.setValue(result.getUser());
                    // Sau khi Auth thành công, fetch profile để lấy role + storeId
                    String uid = result.getUser().getUid();
                    authRepository.getUserProfile(uid)
                            .addOnSuccessListener(user -> {
                                loadingLiveData.setValue(false);
                                userProfileLiveData.setValue(user);
                            })
                            .addOnFailureListener(e -> {
                                loadingLiveData.setValue(false);
                                // Nếu fetch profile lỗi, tạo user mặc định role=customer
                                User fallback = new User();
                                fallback.setId(uid);
                                fallback.setRole("customer");
                                userProfileLiveData.setValue(fallback);
                            });
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(e.getMessage());
                });
    }

    /** Đăng ký */
    public void register(String email, String password, String name, String phone) {
        loadingLiveData.setValue(true);
        authRepository.register(email, password, name, phone)
                .addOnSuccessListener(result -> {
                    loadingLiveData.setValue(false);
                    userLiveData.setValue(result.getUser());
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(e.getMessage());
                });
    }

    /** Đăng xuất */
    public void logout() {
        authRepository.logout();
        userLiveData.setValue(null);
    }
}
