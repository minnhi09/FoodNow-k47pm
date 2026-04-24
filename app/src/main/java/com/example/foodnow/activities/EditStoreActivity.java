package com.example.foodnow.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.foodnow.R;
import com.example.foodnow.repositories.StoreRepository;
import com.example.foodnow.repositories.UserRepository;
import com.example.foodnow.utils.CloudinaryHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

/** Màn hình chỉnh sửa thông tin cửa hàng, mở từ icon cây bút trong OwnerSettingsFragment. */
public class EditStoreActivity extends AppCompatActivity {

    public static final String EXTRA_STORE_ID = "store_id";

    private StoreRepository storeRepo;
    private UserRepository  userRepo;

    private ImageView         imgAvatar;
    private ProgressBar       progressAvatar;
    private TextInputEditText etName, etAddress, etPhone, etDescription;
    private MaterialButton    btnSave;

    private String storeId;
    private ActivityResultLauncher<String> pickAvatarLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_store);

        storeId = getIntent().getStringExtra(EXTRA_STORE_ID);
        if (storeId == null || storeId.isEmpty()) {
            Toast.makeText(this, "Lỗi: không xác định được cửa hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        storeRepo = new StoreRepository();
        userRepo  = new UserRepository();

        imgAvatar      = findViewById(R.id.img_store_avatar);
        progressAvatar = findViewById(R.id.progress_avatar_upload);
        etName         = findViewById(R.id.et_store_name);
        etAddress      = findViewById(R.id.et_store_address);
        etPhone        = findViewById(R.id.et_store_phone);
        etDescription  = findViewById(R.id.et_store_description);
        btnSave        = findViewById(R.id.btn_save_store);

        // Đăng ký launcher chọn ảnh avatar
        pickAvatarLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> { if (uri != null) uploadAvatar(uri); }
        );

        // Nút quay lại
        findViewById(R.id.btn_back_edit_store).setOnClickListener(v -> finish());

        // Click avatar hoặc frame → mở picker
        findViewById(R.id.frame_store_avatar).setOnClickListener(v -> pickAvatarLauncher.launch("image/*"));

        // Load dữ liệu cửa hàng (1 lần)
        storeRepo.getStoreById(storeId).observe(this, store -> {
            if (store == null) return;
            // Pre-fill form
            etName.setText(store.getName());
            etAddress.setText(store.getAddress());
            etPhone.setText(store.getPhone());
            etDescription.setText(store.getDescription());
        });

        // Load avatar chủ cửa hàng (user hiện tại)
        userRepo.getCurrentUser().observe(this, user -> {
            if (user == null) return;
            String url = user.getImageUrl();
            if (url != null && !url.isEmpty()) {
                Glide.with(this)
                        .load(url)
                        .circleCrop()
                        .placeholder(R.drawable.bg_profile_avatar)
                        .into(imgAvatar);
            }
        });

        // Nút Lưu
        btnSave.setOnClickListener(v -> saveStoreInfo());
    }

    private void saveStoreInfo() {
        String name  = etName.getText() != null ? etName.getText().toString().trim() : "";
        String addr  = etAddress.getText() != null ? etAddress.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String desc  = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";

        if (name.isEmpty()) {
            etName.setError("Tên cửa hàng không được để trống");
            etName.requestFocus();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("address", addr);
        updates.put("phone", phone);
        updates.put("description", desc);

        btnSave.setEnabled(false);
        storeRepo.updateStoreFields(storeId, updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đã lưu thông tin cửa hàng", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lưu thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                });
    }

    private void uploadAvatar(Uri uri) {
        progressAvatar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        CloudinaryHelper.uploadImage(this, uri, CloudinaryHelper.FOLDER_PROFILES,
                new CloudinaryHelper.OnUploadCallback() {
                    @Override
                    public void onStart() { /* progress đã hiện */ }

                    @Override
                    public void onSuccess(String secureUrl) {
                        // Cập nhật preview
                        Glide.with(EditStoreActivity.this)
                                .load(secureUrl)
                                .circleCrop()
                                .into(imgAvatar);

                        // Lưu vào Users collection
                        Map<String, Object> avatarUpdate = new HashMap<>();
                        avatarUpdate.put("imageUrl", secureUrl);
                        userRepo.updateUser(avatarUpdate)
                                .addOnSuccessListener(v ->
                                        Toast.makeText(EditStoreActivity.this,
                                                "Đã cập nhật ảnh đại diện", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(EditStoreActivity.this,
                                                "Lưu ảnh thất bại", Toast.LENGTH_SHORT).show());

                        progressAvatar.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(EditStoreActivity.this,
                                "Upload ảnh thất bại: " + errorMessage, Toast.LENGTH_SHORT).show();
                        progressAvatar.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                    }
                });
    }
}
