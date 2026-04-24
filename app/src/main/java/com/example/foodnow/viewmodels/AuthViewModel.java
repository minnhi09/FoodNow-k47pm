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
    // storeId mới sau khi đăng ký store_owner thành công — dùng để route sang StoreOwnerActivity
    private final MutableLiveData<String> storeOwnerRegisteredLiveData = new MutableLiveData<>();

    public AuthViewModel() {
        authRepository = new AuthRepository();
        // Nếu đã đăng nhập trước đó → set user ngay
        if (authRepository.getCurrentUser() != null) {
            userLiveData.setValue(authRepository.getCurrentUser());
        }
    }

    public LiveData<FirebaseUser> getUserLiveData()              { return userLiveData; }
    public LiveData<String> getErrorLiveData()                   { return errorLiveData; }
    public LiveData<Boolean> getLoadingLiveData()                { return loadingLiveData; }
    public LiveData<User> getUserProfileLiveData()               { return userProfileLiveData; }
    public LiveData<String> getStoreOwnerRegisteredLiveData()    { return storeOwnerRegisteredLiveData; }

    /** Đăng nhập */
    public void login(String email, String password) {
        loadingLiveData.setValue(true);
        authRepository.login(email, password)
                .addOnSuccessListener(result -> {
                    userLiveData.setValue(result.getUser());
                    // Sau khi Auth thành công, fetch profile để lấy role + storeId
                    loadUserProfile(result.getUser().getUid());
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(e.getMessage());
                });
    }

    /** Fetch profile — dùng khi login mới hoặc khi đã đăng nhập sẵn */
    public void loadUserProfile(String uid) {
        loadingLiveData.setValue(true);
        authRepository.getUserProfile(uid)
                .addOnSuccessListener(user -> {
                    loadingLiveData.setValue(false);
                    if (user != null) {
                        userProfileLiveData.setValue(user);
                    } else {
                        User fallback = new User();
                        fallback.setId(uid);
                        fallback.setRole("customer");
                        userProfileLiveData.setValue(fallback);
                    }
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    User fallback = new User();
                    fallback.setId(uid);
                    fallback.setRole("customer");
                    userProfileLiveData.setValue(fallback);
                });
    }

    /** Đăng ký tài khoản khách hàng */
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

    /** Đăng ký tài khoản chủ cửa hàng — tạo đồng thời Users + Stores */
    public void registerStoreOwner(String email, String password,
                                   String name, String phone,
                                   String storeName, String storeAddress,
                                   String storePhone, String deliveryTime,
                                   long deliveryFee) {
        loadingLiveData.setValue(true);
        authRepository.registerStoreOwner(email, password, name, phone,
                        storeName, storeAddress, storePhone, deliveryTime, deliveryFee)
                .addOnSuccessListener(storeId -> {
                    loadingLiveData.setValue(false);
                    storeOwnerRegisteredLiveData.setValue(storeId);
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
