package com.example.foodnow.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.foodnow.R;
import com.example.foodnow.activities.ImageAdminActivity;
import com.example.foodnow.activities.LoginActivity;
import com.example.foodnow.utils.CloudinaryHelper;
import com.example.foodnow.viewmodels.AuthViewModel;
import com.example.foodnow.viewmodels.ProfileViewModel;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private AuthViewModel authViewModel;
    private TextInputEditText etName, etPhone, etAddress;
    private ImageView imgAvatar;
    private MaterialButton btnSave, btnLogout, btnChangeAvatar, btnManageImages;

    // ① Khai báo launcher chọn ảnh từ máy
    private ActivityResultLauncher<String> pickImageLauncher;

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
        btnSave = view.findViewById(R.id.btn_save_profile);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnChangeAvatar = view.findViewById(R.id.btn_change_avatar);
        btnManageImages = view.findViewById(R.id.btn_manage_images);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        authViewModel    = new ViewModelProvider(this).get(AuthViewModel.class);

        // ② Đăng ký launcher
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) uploadAvatar(uri);
                }
        );

        // ③ Nút đổi ảnh
        btnChangeAvatar.setOnClickListener(v -> {
            pickImageLauncher.launch("image/*");
        });

        // ④ Nút quản trị upload ảnh Store/Food
        btnManageImages.setOnClickListener(v -> openImageAdminScreen());

        // Quan sát thông tin user
        profileViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;
            etName.setText(user.getName());
            etPhone.setText(user.getPhone());
            etAddress.setText(user.getAddress());

            // Chỉ admin mới thấy nút Quản trị
            boolean isAdmin = "admin".equals(user.getRole());
            btnManageImages.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

            if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                Glide.with(requireContext()).load(user.getImageUrl())
                        .placeholder(R.mipmap.ic_launcher).into(imgAvatar);
            }
        });

        // Lưu thông tin profile
        btnSave.setOnClickListener(v -> {
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", etName.getText().toString().trim());
            updates.put("phone", etPhone.getText().toString().trim());
            updates.put("address", etAddress.getText().toString().trim());
            Task<Void> updateTask = profileViewModel.updateUser(updates);
            updateTask.addOnSuccessListener(unused -> showToast("Đã lưu thông tin"))
                    .addOnFailureListener(error ->
                            showToast("Lưu thông tin thất bại: " + getErrorMessage(error)));
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

    private void uploadAvatar(Uri imageUri) {
        CloudinaryHelper.uploadImage(requireContext(), imageUri, CloudinaryHelper.FOLDER_PROFILES,
                new CloudinaryHelper.OnUploadCallback() {
                    @Override
                    public void onStart() {
                        setAvatarActionsEnabled(false);
                        showToast("Đang upload ảnh đại diện...");
                    }

                    @Override
                    public void onSuccess(String secureUrl) {
                        if (!isAdded()) return;
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("imageUrl", secureUrl);
                        profileViewModel.updateUser(updates)
                                .addOnSuccessListener(unused -> {
                                    if (!isAdded()) return;
                                    Glide.with(requireContext())
                                            .load(secureUrl)
                                            .placeholder(R.mipmap.ic_launcher)
                                            .into(imgAvatar);
                                    showToast("Đã cập nhật ảnh đại diện");
                                })
                                .addOnFailureListener(error -> {
                                    if (!isAdded()) return;
                                    showToast("Lưu ảnh đại diện thất bại: " + getErrorMessage(error));
                                })
                                .addOnCompleteListener(task -> {
                                    if (!isAdded()) return;
                                    setAvatarActionsEnabled(true);
                                });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        if (!isAdded()) return;
                        setAvatarActionsEnabled(true);
                        showToast("Upload ảnh thất bại: " + errorMessage);
                    }
                });
    }

    private void openImageAdminScreen() {
        Intent intent = new Intent(requireContext(), ImageAdminActivity.class);
        startActivity(intent);
    }

    private void setAvatarActionsEnabled(boolean isEnabled) {
        btnChangeAvatar.setEnabled(isEnabled);
        btnSave.setEnabled(isEnabled);
        btnManageImages.setEnabled(isEnabled);
        btnLogout.setEnabled(isEnabled);
    }

    private void showToast(String message) {
        if (getContext() == null) return;
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private String getErrorMessage(@NonNull Exception error) {
        if (error.getMessage() == null || error.getMessage().trim().isEmpty()) {
            return "Đã xảy ra lỗi";
        }
        return error.getMessage();
    }
}
