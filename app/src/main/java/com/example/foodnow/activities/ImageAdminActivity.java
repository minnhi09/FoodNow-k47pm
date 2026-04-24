package com.example.foodnow.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.foodnow.R;
import com.example.foodnow.utils.CloudinaryHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ImageAdminActivity extends AppCompatActivity {

    private static final String TYPE_STORE = "Store";
    private static final String TYPE_FOOD = "Food";
    private static final String COLLECTION_STORES = "Stores";
    private static final String COLLECTION_FOODS = "Foods";

    private Spinner spTargetType;
    private Spinner spTargetItem;
    private ImageView imgPreview;
    private TextView tvSelectedImage;
    private MaterialButton btnChooseImage;
    private MaterialButton btnUploadImage;
    private MaterialButton btnReloadTargets;
    private ProgressBar progressBar;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final List<AdminTargetItem> targetItems = new ArrayList<>();
    private ArrayAdapter<AdminTargetItem> targetItemAdapter;
    private ActivityResultLauncher<String> pickImageLauncher;
    private Uri selectedImageUri;

    private boolean isLoadingTargets = false;
    private boolean isUploading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_image_admin);

        spTargetType = findViewById(R.id.sp_target_type);
        spTargetItem = findViewById(R.id.sp_target_item);
        imgPreview = findViewById(R.id.img_admin_preview);
        tvSelectedImage = findViewById(R.id.tv_selected_image);
        btnChooseImage = findViewById(R.id.btn_choose_image);
        btnUploadImage = findViewById(R.id.btn_upload_image);
        btnReloadTargets = findViewById(R.id.btn_reload_targets);
        progressBar = findViewById(R.id.progress_admin);

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri == null) return;
            selectedImageUri = uri;
            Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(imgPreview);
            tvSelectedImage.setText("Đã chọn: " + getReadableImageName(uri));
            refreshUiState();
        });

        checkAdminAndInit();
    }

    /** Kiểm tra quyền admin từ Firestore trước khi cho phép dùng màn hình */
    private void checkAdminAndInit() {
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null
                ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (uid == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Disable UI trong khi kiểm tra
        setAllActionsEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        db.collection("Users").document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    progressBar.setVisibility(View.GONE);
                    if (!snapshot.exists()) {
                        Toast.makeText(this, "Không tìm thấy tài khoản", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    String role = snapshot.getString("role");
                    if (!"admin".equals(role)) {
                        Toast.makeText(this, "Bạn không có quyền truy cập", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    // Qua kiểm tra → khởi tạo bình thường
                    setAllActionsEnabled(true);
                    setupTargetTypeSpinner();
                    setupTargetItemSpinner();
                    btnChooseImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
                    btnUploadImage.setOnClickListener(v -> uploadSelectedImage());
                    btnReloadTargets.setOnClickListener(v -> loadTargetsForType(getCurrentType()));
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Kiểm tra quyền thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void setAllActionsEnabled(boolean enabled) {
        spTargetType.setEnabled(enabled);
        spTargetItem.setEnabled(enabled);
        btnChooseImage.setEnabled(enabled);
        btnUploadImage.setEnabled(enabled);
        btnReloadTargets.setEnabled(enabled);
    }

    private void setupTargetTypeSpinner() {
        List<String> targetTypes = new ArrayList<>();
        targetTypes.add(TYPE_STORE);
        targetTypes.add(TYPE_FOOD);

        ArrayAdapter<String> targetTypeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                targetTypes
        );
        targetTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTargetType.setAdapter(targetTypeAdapter);
        spTargetType.setSelection(0);
        spTargetType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadTargetsForType(getCurrentType());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupTargetItemSpinner() {
        targetItemAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                targetItems
        );
        targetItemAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTargetItem.setAdapter(targetItemAdapter);
    }

    private void loadTargetsForType(String type) {
        isLoadingTargets = true;
        refreshUiState();
        targetItems.clear();
        targetItemAdapter.notifyDataSetChanged();

        db.collection(getCollectionByType(type))
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String displayName = resolveDisplayName(type, document);
                        targetItems.add(new AdminTargetItem(document.getId(), displayName));
                    }
                    targetItemAdapter.notifyDataSetChanged();
                    if (targetItems.isEmpty()) {
                        showToast("Không có dữ liệu trong " + getCollectionByType(type));
                    }
                })
                .addOnFailureListener(error ->
                        showToast("Tải danh sách thất bại: " + getErrorMessage(error)))
                .addOnCompleteListener(task -> {
                    isLoadingTargets = false;
                    refreshUiState();
                });
    }

    private void uploadSelectedImage() {
        if (selectedImageUri == null) {
            showToast("Bạn chưa chọn ảnh");
            return;
        }
        AdminTargetItem selectedTarget = (AdminTargetItem) spTargetItem.getSelectedItem();
        if (selectedTarget == null) {
            showToast("Danh sách Store/Food đang trống");
            return;
        }

        String currentType = getCurrentType();
        String folder = TYPE_STORE.equals(currentType)
                ? CloudinaryHelper.FOLDER_STORES
                : CloudinaryHelper.FOLDER_FOODS;

        CloudinaryHelper.uploadImage(this, selectedImageUri, folder, new CloudinaryHelper.OnUploadCallback() {
            @Override
            public void onStart() {
                isUploading = true;
                refreshUiState();
                showToast("Đang upload ảnh...");
            }

            @Override
            public void onSuccess(String secureUrl) {
                updateItemImageUrl(currentType, selectedTarget, secureUrl);
            }

            @Override
            public void onError(String errorMessage) {
                isUploading = false;
                refreshUiState();
                showToast("Upload ảnh thất bại: " + errorMessage);
            }
        });
    }

    private void updateItemImageUrl(String type, AdminTargetItem selectedTarget, String imageUrl) {
        db.collection(getCollectionByType(type))
                .document(selectedTarget.id)
                .update("imageUrl", imageUrl)
                .addOnSuccessListener(unused -> {
                    selectedImageUri = null;
                    tvSelectedImage.setText("Chưa chọn ảnh");
                    imgPreview.setImageResource(R.drawable.ic_launcher_background);
                    showToast("Đã cập nhật ảnh cho " + selectedTarget.name);
                })
                .addOnFailureListener(error ->
                        showToast("Cập nhật Firestore thất bại: " + getErrorMessage(error)))
                .addOnCompleteListener(task -> {
                    isUploading = false;
                    refreshUiState();
                });
    }

    private String resolveDisplayName(String type, DocumentSnapshot document) {
        String displayName;
        if (TYPE_STORE.equals(type)) {
            displayName = document.getString("name");
        } else {
            displayName = document.getString("title");
            if (isBlank(displayName)) {
                displayName = document.getString("name");
            }
        }
        if (isBlank(displayName)) {
            return "(Không tên)";
        }
        return displayName;
    }

    private String getCollectionByType(String type) {
        return TYPE_STORE.equals(type) ? COLLECTION_STORES : COLLECTION_FOODS;
    }

    private String getCurrentType() {
        Object selectedItem = spTargetType.getSelectedItem();
        if (selectedItem == null) {
            return TYPE_STORE;
        }
        return selectedItem.toString();
    }

    private void refreshUiState() {
        boolean isBusy = isLoadingTargets || isUploading;
        progressBar.setVisibility(isBusy ? View.VISIBLE : View.GONE);
        spTargetType.setEnabled(!isBusy);
        spTargetItem.setEnabled(!isBusy && !targetItems.isEmpty());
        btnChooseImage.setEnabled(!isBusy);
        btnReloadTargets.setEnabled(!isBusy);
        btnUploadImage.setEnabled(!isBusy && selectedImageUri != null && !targetItems.isEmpty());
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String getReadableImageName(Uri uri) {
        String lastSegment = uri.getLastPathSegment();
        if (isBlank(lastSegment)) {
            return "Ảnh đã chọn";
        }
        return lastSegment;
    }

    private String getErrorMessage(@NonNull Exception error) {
        if (isBlank(error.getMessage())) {
            return "Đã xảy ra lỗi";
        }
        return error.getMessage();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static class AdminTargetItem {
        private final String id;
        private final String name;

        private AdminTargetItem(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @NonNull
        @Override
        public String toString() {
            return name + " (" + id + ")";
        }
    }
}
