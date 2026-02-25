package com.example.foodnow.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodnow.models.User;
import com.example.foodnow.repositories.UserRepository;

import java.util.Map;

public class ProfileViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final LiveData<User> user;

    public ProfileViewModel() {
        userRepository = new UserRepository();
        user = userRepository.getCurrentUser();
    }

    public LiveData<User> getUser() { return user; }

    public void updateUser(Map<String, Object> updates) {
        userRepository.updateUser(updates);
    }
}
