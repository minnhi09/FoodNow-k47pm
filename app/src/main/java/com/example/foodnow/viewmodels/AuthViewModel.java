package com.example.foodnow.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodnow.repositories.AuthRepository;
import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;

    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);

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

    /** Đăng nhập */
    public void login(String email, String password) {
        loadingLiveData.setValue(true);
        authRepository.login(email, password)
                .addOnSuccessListener(result -> {
                    loadingLiveData.setValue(false);
                    userLiveData.setValue(result.getUser());
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
