package com.example.foodnow.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.foodnow.R;
import com.example.foodnow.models.Category;
import com.example.foodnow.repositories.CategoryRepository;
import com.example.foodnow.repositories.StoreRepository;
import com.example.foodnow.repositories.UserRepository;
import com.example.foodnow.utils.CloudinaryHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Màn hình chỉnh sửa thông tin cửa hàng, mở từ icon cây bút trong OwnerSettingsFragment. */
public class EditStoreActivity extends AppCompatActivity {

    public static final String EXTRA_STORE_ID = "store_id";

    private StoreRepository storeRepo;
    private UserRepository  userRepo;
    private CategoryRepository categoryRepo;

    private ImageView         imgAvatar;
    private ProgressBar       progressAvatar;
    private TextInputEditText etName, etAddress, etPhone, etDescription;
    private AutoCompleteTextView actvCategory;
    private MaterialButton    btnSave;

    private String storeId;
    private String currentCategoryId = "";
    private final List<Category> categoryList = new ArrayList<>();
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
        categoryRepo = new CategoryRepository();

        imgAvatar      = findViewById(R.id.img_store_avatar);
        progressAvatar = findViewById(R.id.progress_avatar_upload);
        etName         = findViewById(R.id.et_store_name);
        etAddress      = findViewById(R.id.et_store_address);
        etPhone        = findViewById(R.id.et_store_phone);
        etDescription  = findViewById(R.id.et_store_description);
        actvCategory   = findViewById(R.id.actv_category);
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
            // Pre-select category
            currentCategoryId = store.getCategoryId() != null ? store.getCategoryId() : "";
            preselectCategory(currentCategoryId);
        });

        // Load danh mục cho dropdown
        categoryRepo.getAllCategories().observe(this, categories -> {
            categoryList.clear();
            if (categories != null) categoryList.addAll(categories);
            List<String> names = new ArrayList<>();
            for (Category c : categoryList) names.add(c.getName());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, names);
            actvCategory.setAdapter(adapter);
            // Nếu đã có categoryId trước khi categories load xong, pre-select lại
            preselectCategory(currentCategoryId);
            actvCategory.setOnItemClickListener((parent, view, position, id) ->
                    currentCategoryId = categoryList.get(position).getId());
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
        if (!currentCategoryId.isEmpty()) {
            updates.put("categoryId", currentCategoryId);
        }

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

    /** Hiển thị tên danh mục tương ứng với categoryId đã lưu trong AutoCompleteTextView */
    private void preselectCategory(String categoryId) {
        if (categoryId == null || categoryId.isEmpty()) return;
        for (Category c : categoryList) {
            if (c.getId().equals(categoryId)) {
                actvCategory.setText(c.getName(), false);
                break;
            }
        }
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
