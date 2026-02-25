package com.example.foodnow.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.foodnow.R;
import com.example.foodnow.activities.LoginActivity;
import com.example.foodnow.viewmodels.AuthViewModel;
import com.example.foodnow.viewmodels.ProfileViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private AuthViewModel authViewModel;
    private TextInputEditText etName, etPhone, etAddress;
    private ImageView imgAvatar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        etName    = view.findViewById(R.id.et_profile_name);
        etPhone   = view.findViewById(R.id.et_profile_phone);
        etAddress = view.findViewById(R.id.et_profile_address);
        imgAvatar = view.findViewById(R.id.img_avatar);
        MaterialButton btnSave   = view.findViewById(R.id.btn_save_profile);
        MaterialButton btnLogout = view.findViewById(R.id.btn_logout);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        authViewModel    = new ViewModelProvider(this).get(AuthViewModel.class);

        // Quan sát thông tin user
        profileViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;
            etName.setText(user.getName());
            etPhone.setText(user.getPhone());
            etAddress.setText(user.getAddress());

            if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                Glide.with(requireContext()).load(user.getImageUrl())
                        .placeholder(R.mipmap.ic_launcher).into(imgAvatar);
            }
        });

        // Lưu thông tin
        btnSave.setOnClickListener(v -> {
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", etName.getText().toString().trim());
            updates.put("phone", etPhone.getText().toString().trim());
            updates.put("address", etAddress.getText().toString().trim());
            profileViewModel.updateUser(updates);
            Toast.makeText(getContext(), "Đã lưu thông tin", Toast.LENGTH_SHORT).show();
        });

        // Đăng xuất
        btnLogout.setOnClickListener(v -> {
            authViewModel.logout();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }
}
