package com.example.foodnow.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodnow.R;
import com.example.foodnow.adapters.FoodAdapter;
import com.example.foodnow.models.Food;
import com.example.foodnow.models.Favorite;
import com.example.foodnow.repositories.FavoriteRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.example.foodnow.viewmodels.StoreDetailViewModel;
import com.example.foodnow.utils.CartManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StoreDetailActivity extends AppCompatActivity {

    // ── Khai báo biến ────────────────────────────────────
    private StoreDetailViewModel viewModel;

    private ImageView imgStore;
    private ImageView btnBack;
    private ImageView btnFavorite;
    private TextView tvName, tvRating, tvTime, tvFee;
    private RecyclerView rvFoods;

    private FoodAdapter foodAdapter;
    private final List<Food> foodList = new ArrayList<>();

    // Favorite
    private FavoriteRepository favoriteRepository;
    private boolean isFavorite = false;
    private String favoriteId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_detail);

        // ① Ánh xạ view
        imgStore    = findViewById(R.id.img_store_detail);
        btnBack     = findViewById(R.id.btn_back);
        btnFavorite = findViewById(R.id.btn_favorite);
        tvName      = findViewById(R.id.tv_store_detail_name);
        tvRating = findViewById(R.id.tv_store_detail_rating);
        tvTime = findViewById(R.id.tv_store_detail_time);
        tvFee = findViewById(R.id.tv_store_detail_fee);
        rvFoods = findViewById(R.id.rv_foods);

        // ② Nhận dữ liệu quán từ Intent
        String storeId = getIntent().getStringExtra("storeId");
        String storeName = getIntent().getStringExtra("storeName");
        String storeImage = getIntent().getStringExtra("storeImage");
        float storeRating = getIntent().getFloatExtra("storeRating", 0f);
        String storeTime = getIntent().getStringExtra("storeDeliveryTime");
        long storeDeliveryFee = getIntent().getLongExtra("storeDeliveryFee", 0L);

        // ③ Hiển thị thông tin quán lên header
        tvName.setText(storeName != null ? storeName : "Quán ăn");
        tvRating.setText(String.format(Locale.US, "%.1f", storeRating));
        tvTime.setText(storeTime != null ? storeTime : "Đang cập nhật");
        tvFee.setText(formatDeliveryFee(storeDeliveryFee));

        Glide.with(this)
                .load(storeImage)
                .placeholder(R.drawable.ic_launcher_background)
                .into(imgStore);

        // ④ Nút back
        btnBack.setOnClickListener(v -> finish());

        // ⑤ Nút yêu thích
        favoriteRepository = new FavoriteRepository();
        if (storeId != null && !storeId.isEmpty()) {
            loadFavoriteState(storeId);
        }
        final String finalStoreImage = storeImage;
        final String finalStoreName2 = storeName;
        final String finalStoreId = storeId;
        btnFavorite.setOnClickListener(v ->
                toggleFavorite(finalStoreId, finalStoreName2, finalStoreImage));

        // ⑤ Setup RecyclerView danh sách món
        foodAdapter = new FoodAdapter(this, foodList, food -> {
            CartManager.AddResult result = CartManager.getInstance().addFood(
                    food, 1,
                    storeId != null ? storeId : "",
                    storeName != null ? storeName : "",
                    storeDeliveryFee);
            if (result == CartManager.AddResult.STORE_MISMATCH) {
                showStoreMismatchDialog(food,
                        storeId != null ? storeId : "",
                        storeName != null ? storeName : "",
                        storeDeliveryFee);
                return;
            }
            Toast.makeText(this, "Đã thêm: " + food.getTitle(), Toast.LENGTH_SHORT).show();
        });

        // Click vào card món → mở FoodDetailActivity
        final String finalStoreName = storeName;
        final String finalStoreTime = storeTime;
        final long finalDeliveryFee = storeDeliveryFee;
        foodAdapter.setOnFoodClickListener(food -> {
            Intent intent = new Intent(this, FoodDetailActivity.class);
            intent.putExtra("foodId",          food.getId());
            intent.putExtra("foodTitle",        food.getTitle());
            intent.putExtra("foodDescription",  food.getDescription());
            intent.putExtra("foodPrice",        food.getPrice());
            intent.putExtra("foodImageUrl",     food.getImageUrl());
            intent.putExtra("foodRating",       food.getRating());
            intent.putExtra("foodStoreId",      storeId);
            intent.putExtra("storeName",        finalStoreName);
            intent.putExtra("storeDeliveryTime",finalStoreTime);
            intent.putExtra("storeDeliveryFee", finalDeliveryFee);
            startActivity(intent);
        });

        rvFoods.setLayoutManager(new LinearLayoutManager(this));
        rvFoods.setAdapter(foodAdapter);

        // ⑥ Khởi tạo ViewModel và observe danh sách món
        viewModel = new ViewModelProvider(this).get(StoreDetailViewModel.class);

        if (storeId != null && !storeId.isEmpty()) {
            viewModel.getFoods(storeId).observe(this, foods -> {
                foodList.clear();
                if (foods != null && !foods.isEmpty()) {
                    foodList.addAll(foods);
                }
                foodAdapter.notifyDataSetChanged();
            });
        }
    }

    // Hỏi xác nhận khi thêm món từ quán khác
    private void showStoreMismatchDialog(Food food, String sId, String sName, long sFee) {
        String currentStore = CartManager.getInstance().getStoreName();
        new AlertDialog.Builder(this)
                .setTitle("Bắt đầu đơn mới?")
                .setMessage("Giỏ hàng đang có món từ \"" + currentStore
                        + "\".\nXóa giỏ và thêm từ \"" + sName + "\" không?")
                .setPositiveButton("Xóa và thêm", (dialog, which) -> {
                    CartManager.getInstance().clear();
                    CartManager.getInstance().addFood(food, 1, sId, sName, sFee);
                    Toast.makeText(this, "Đã thêm: " + food.getTitle(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Định dạng phí giao hàng
    private String formatDeliveryFee(long fee) {
        if (fee <= 0) return "Miễn phí ship";
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        return "Ship " + nf.format(fee) + "đ";
    }

    // ── Favorite ─────────────────────────────────────────

    private void loadFavoriteState(String storeId) {
        String uid = getCurrentUid();
        if (uid == null) return;
        favoriteRepository.checkIsFavorite(uid, "store", storeId)
                .observe(this, docId -> {
                    favoriteId = docId;
                    isFavorite = docId != null;
                    updateFavoriteIcon();
                });
    }

    private void toggleFavorite(String storeId, String storeName, String storeImage) {
        String uid = getCurrentUid();
        if (uid == null) {
            Toast.makeText(this, "Bạn cần đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isFavorite && favoriteId != null) {
            favoriteRepository.removeFavorite(favoriteId)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(this, "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Favorite fav = new Favorite(uid, "store", storeId,
                    storeName != null ? storeName : "",
                    storeImage != null ? storeImage : "");
            favoriteRepository.addFavorite(fav)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void updateFavoriteIcon() {
        int tintColor = isFavorite ? R.color.home_badge_red : R.color.white;
        btnFavorite.setColorFilter(getColor(tintColor));
    }

    private String getCurrentUid() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return null;
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
