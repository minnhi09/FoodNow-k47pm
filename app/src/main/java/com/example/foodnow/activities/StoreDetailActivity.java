package com.example.foodnow.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodnow.R;
import com.example.foodnow.adapters.FoodAdapter;
import com.example.foodnow.models.Food;
import com.example.foodnow.viewmodels.FavoritesViewModel;
import com.example.foodnow.viewmodels.StoreDetailViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StoreDetailActivity extends AppCompatActivity {

    // ── Khai báo biến ────────────────────────────────────
    private StoreDetailViewModel viewModel;
    private FavoritesViewModel favoritesViewModel;

    private ImageView imgStore;
    private ImageView btnBack, btnFavorite;
    private TextView tvName, tvRating, tvTime, tvFee;
    private RecyclerView rvFoods;

    private FoodAdapter foodAdapter;
    private final List<Food> foodList = new ArrayList<>();

    private boolean isFavorite = false;
    private String currentFavoriteId = "";
    private String storeId = "";
    private String storeName = "";
    private String storeImage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_detail);

        // ① Ánh xạ view
        imgStore = findViewById(R.id.img_store_detail);
        btnBack = findViewById(R.id.btn_back);
        btnFavorite = findViewById(R.id.btn_favorite_store);
        tvName = findViewById(R.id.tv_store_detail_name);
        tvRating = findViewById(R.id.tv_store_detail_rating);
        tvTime = findViewById(R.id.tv_store_detail_time);
        tvFee = findViewById(R.id.tv_store_detail_fee);
        rvFoods = findViewById(R.id.rv_foods);

        // ② Nhận dữ liệu quán từ Intent
        storeId = getIntent().getStringExtra("storeId");
        storeName = getIntent().getStringExtra("storeName");
        storeImage = getIntent().getStringExtra("storeImage");
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

        // ④ Setup Favorites logic
        setupFavorites();

        // ⑤ Nút back
        btnBack.setOnClickListener(v -> finish());

        // ⑥ Setup RecyclerView danh sách món
        foodAdapter = new FoodAdapter(this, foodList, food -> {
            Toast.makeText(this, "Đã thêm: " + food.getTitle(), Toast.LENGTH_SHORT).show();
        });

        // Click vào card món → mở FoodDetailActivity
        foodAdapter.setOnFoodClickListener(food -> {
            Intent intent = new Intent(this, FoodDetailActivity.class);
            intent.putExtra("foodId",          food.getId());
            intent.putExtra("foodTitle",        food.getTitle());
            intent.putExtra("foodDescription",  food.getDescription());
            intent.putExtra("foodPrice",        food.getPrice());
            intent.putExtra("foodImageUrl",     food.getImageUrl());
            intent.putExtra("foodRating",       food.getRating());
            intent.putExtra("storeId",          storeId);
            intent.putExtra("storeName",        storeName);
            intent.putExtra("storeDeliveryTime",storeTime);
            intent.putExtra("storeDeliveryFee", storeDeliveryFee);
            startActivity(intent);
        });

        rvFoods.setLayoutManager(new LinearLayoutManager(this));
        rvFoods.setAdapter(foodAdapter);

        // ⑦ Khởi tạo ViewModel và observe danh sách món
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

    private void setupFavorites() {
        favoritesViewModel = new ViewModelProvider(this).get(FavoritesViewModel.class);
        
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && storeId != null) {
            favoritesViewModel.checkFavorite(user.getUid(), storeId, "store");
        }

        favoritesViewModel.getItemFavoritedLiveData().observe(this, favorited -> {
            isFavorite = Boolean.TRUE.equals(favorited);
            updateFavoriteIcon();
        });

        favoritesViewModel.getCurrentFavoriteIdLiveData().observe(this, id -> {
            currentFavoriteId = id != null ? id : "";
        });

        btnFavorite.setOnClickListener(v -> {
            if (user == null) {
                Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isFavorite) {
                if (!currentFavoriteId.isEmpty()) {
                    favoritesViewModel.removeFavorite(currentFavoriteId);
                    Toast.makeText(this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                }
            } else {
                favoritesViewModel.addFavorite(user.getUid(), "store", storeId, storeName, storeImage);
                Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFavoriteIcon() {
        btnFavorite.setImageResource(isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        // Tắt màu lọc để dùng màu gốc của vector heart
        btnFavorite.setColorFilter(null);
    }

    // Định dạng phí giao hàng
    private String formatDeliveryFee(long fee) {
        if (fee <= 0) return "Miễn phí ship";
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        return "Ship " + nf.format(fee) + "đ";
    }
}
