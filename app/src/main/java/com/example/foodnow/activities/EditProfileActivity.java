package com.example.foodnow.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.foodnow.R;
import com.example.foodnow.utils.CloudinaryHelper;
import com.example.foodnow.viewmodels.ProfileViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private ProfileViewModel profileViewModel;
    private ImageView imgAvatar;
    private TextInputEditText etName, etPhone, etAddress;
    private MaterialButton btnSave;

    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        imgAvatar = findViewById(R.id.img_avatar);
        etName    = findViewById(R.id.et_profile_name);
        etPhone   = findViewById(R.id.et_profile_phone);
        etAddress = findViewById(R.id.et_profile_address);
        btnSave   = findViewById(R.id.btn_save_profile);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Đăng ký launcher chọn ảnh
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> { if (uri != null) uploadAvatar(uri); }
        );

        // Click avatar → mở picker ảnh
        findViewById(R.id.frame_avatar).setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        imgAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // Nút quay lại
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Quan sát dữ liệu user
        profileViewModel.getUser().observe(this, user -> {
            if (user == null) return;
            etName.setText(user.getName());
            etPhone.setText(user.getPhone());
            etAddress.setText(user.getAddress());

            if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                Glide.with(this)
                        .load(user.getImageUrl())
                        .circleCrop()
                        .placeholder(R.drawable.bg_profile_avatar)
                        .into(imgAvatar);
            }
        });

        // Lưu thông tin
        btnSave.setOnClickListener(v -> {
            String name    = etName.getText() != null ? etName.getText().toString().trim() : "";
            String phone   = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
            String address = etAddress.getText() != null ? etAddress.getText().toString().trim() : "";

            Map<String, Object> updates = new HashMap<>();
            updates.put("name", name);
            updates.put("phone", phone);
            updates.put("address", address);

            btnSave.setEnabled(false);
            profileViewModel.updateUser(updates)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Đã lưu thông tin", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(error -> {
                        Toast.makeText(this, "Lưu thông tin thất bại", Toast.LENGTH_SHORT).show();
                        btnSave.setEnabled(true);
                    });
        });
    }

    private void uploadAvatar(Uri imageUri) {
        CloudinaryHelper.uploadImage(this, imageUri, CloudinaryHelper.FOLDER_PROFILES,
                new CloudinaryHelper.OnUploadCallback() {
                    @Override
                    public void onStart() {
                        btnSave.setEnabled(false);
                        Toast.makeText(EditProfileActivity.this, "Đang upload ảnh...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(String secureUrl) {
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("imageUrl", secureUrl);
                        profileViewModel.updateUser(updates)
                                .addOnSuccessListener(unused -> {
                                    Glide.with(EditProfileActivity.this)
                                            .load(secureUrl)
                                            .circleCrop()
                                            .into(imgAvatar);
                                    Toast.makeText(EditProfileActivity.this, "Đã cập nhật ảnh đại diện", Toast.LENGTH_SHORT).show();
                                    btnSave.setEnabled(true);
                                })
                                .addOnFailureListener(error -> {
                                    Toast.makeText(EditProfileActivity.this, "Lưu ảnh thất bại", Toast.LENGTH_SHORT).show();
                                    btnSave.setEnabled(true);
                                });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(EditProfileActivity.this, "Upload ảnh thất bại", Toast.LENGTH_SHORT).show();
                        btnSave.setEnabled(true);
                    }
                });
    }
}
