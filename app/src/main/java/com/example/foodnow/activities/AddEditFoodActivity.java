package com.example.foodnow.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.foodnow.R;
import com.example.foodnow.models.Food;
import com.example.foodnow.utils.CloudinaryHelper;
import com.example.foodnow.viewmodels.StoreOwnerViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Màn hình thêm / sửa món ăn.
 *
 * Chế độ THÊM MỚI: gọi từ FAB trong ManageFoodsFragment, không có "foodId".
 * Chế độ CHỈNH SỬA: gọi từ nút "Sửa" trong ManageFoodAdapter, có "foodId".
 *
 * Luồng ảnh: chọn ảnh từ thư viện → upload Cloudinary → lấy URL → lưu vào Firestore.
 */
public class AddEditFoodActivity extends AppCompatActivity {

    // ViewModel dùng chung với StoreOwnerActivity (tạo mới riêng vì Activity khác)
    private StoreOwnerViewModel viewModel;

    private TextInputEditText etTitle, etDescription, etPrice, etImageUrl;
    private ImageView imgPreview;
    private Switch switchAvailable;
    private ProgressBar progressBar;
    private MaterialButton btnSave, btnPickImage;

    private String storeId   = "";
    private String foodId    = "";     // rỗng = thêm mới
    private String uploadedImageUrl = "";

    // Launcher chọn ảnh từ thư viện
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) uploadImage(uri);
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_food);

        // Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // ViewModel (không dùng requireActivity() vì đây là Activity độc lập)
        viewModel = new ViewModelProvider(this).get(StoreOwnerViewModel.class);

        // Ánh xạ views
        etTitle       = findViewById(R.id.et_food_title);
        etDescription = findViewById(R.id.et_food_description);
        etPrice       = findViewById(R.id.et_food_price);
        etImageUrl    = findViewById(R.id.et_food_image_url);
        imgPreview    = findViewById(R.id.img_food_preview);
        switchAvailable = findViewById(R.id.switch_available);
        progressBar   = findViewById(R.id.progress_bar);
        btnSave       = findViewById(R.id.btn_save_food);
        btnPickImage  = findViewById(R.id.btn_pick_image);

        // Nhận Intent extras
        Intent intent = getIntent();
        storeId = intent.getStringExtra("storeId");
        if (storeId == null) storeId = "";
        foodId  = intent.getStringExtra("foodId");
        if (foodId == null) foodId = "";

        boolean isEdit = !foodId.isEmpty();
        setTitle(isEdit ? "Sửa món ăn" : "Thêm món ăn");

        // Nếu sửa → điền sẵn dữ liệu
        if (isEdit) {
            etTitle.setText(intent.getStringExtra("foodTitle"));
            etDescription.setText(intent.getStringExtra("foodDescription"));
            etPrice.setText(String.valueOf(intent.getLongExtra("foodPrice", 0)));
            uploadedImageUrl = intent.getStringExtra("foodImageUrl");
            if (uploadedImageUrl == null) uploadedImageUrl = "";
            etImageUrl.setText(uploadedImageUrl);
            switchAvailable.setChecked(intent.getBooleanExtra("foodAvailable", true));

            // Hiển thị ảnh hiện tại
            if (!uploadedImageUrl.isEmpty()) {
                Glide.with(this).load(uploadedImageUrl).into(imgPreview);
            }
        }

        // Quan sát loading
        viewModel.getLoading().observe(this, loading -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            btnSave.setEnabled(!loading);
        });

        // Quan sát kết quả thao tác
        viewModel.getActionMessage().observe(this, msg -> {
            if (msg == null) return;
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            if (msg.contains("thành công")) finish();
        });

        btnPickImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnSave.setOnClickListener(v -> saveFood());
    }

    private void uploadImage(Uri uri) {
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        CloudinaryHelper.uploadImage(this, uri, CloudinaryHelper.FOLDER_FOODS,
                new CloudinaryHelper.OnUploadCallback() {
                    @Override public void onStart() {}
                    @Override
                    public void onSuccess(String secureUrl) {
                        uploadedImageUrl = secureUrl;
                        etImageUrl.setText(secureUrl);
                        Glide.with(AddEditFoodActivity.this).load(secureUrl).into(imgPreview);
                        progressBar.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                    }
                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(AddEditFoodActivity.this,
                                "Upload ảnh lỗi: " + errorMessage, Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                    }
                });
    }

    private void saveFood() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String desc  = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String priceStr = etPrice.getText() != null ? etPrice.getText().toString().trim() : "";
        String imgUrl   = etImageUrl.getText() != null ? etImageUrl.getText().toString().trim() : "";

        if (title.isEmpty()) {
            etTitle.setError("Vui lòng nhập tên món ăn");
            return;
        }
        if (priceStr.isEmpty()) {
            etPrice.setError("Vui lòng nhập giá");
            return;
        }

        long price;
        try {
            price = Long.parseLong(priceStr);
        } catch (NumberFormatException e) {
            etPrice.setError("Giá không hợp lệ");
            return;
        }

        // Ưu tiên URL đã upload, fallback sang text nhập tay
        String finalImageUrl = uploadedImageUrl.isEmpty() ? imgUrl : uploadedImageUrl;

        Food food = new Food();
        food.setTitle(title);
        food.setDescription(desc);
        food.setPrice(price);
        food.setImageUrl(finalImageUrl);
        food.setStoreId(storeId);
        food.setRating(0f);
        food.setAvailable(switchAvailable.isChecked());

        if (!foodId.isEmpty()) {
            // Chỉnh sửa
            food.setId(foodId);
            viewModel.updateFood(food);
        } else {
            // Thêm mới
            viewModel.addFood(food);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
