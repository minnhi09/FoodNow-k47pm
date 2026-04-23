package com.example.foodnow.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodnow.R;
import com.example.foodnow.adapters.ReviewAdapter;
import com.example.foodnow.models.Food;
import com.example.foodnow.models.Review;
import com.example.foodnow.utils.CartManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoodDetailActivity extends AppCompatActivity {

    // ── UI views ─────────────────────────────────────────
    private ImageView imgFood, ivFavoriteIcon;
    private TextView tvTitle, tvRating, tvReviewCount, tvPrice, tvDescription;
    private TextView tvStoreName, tvStoreDeliveryTime;
    private TextView tvFoodCategory, tvPopularBadge;
    private TextView btnDecrease, btnIncrease, tvQuantity;
    private TextView btnOrder;
    private android.view.View btnBack, btnFavorite, btnAddToCart;
    private RecyclerView rvReviews;

    // ── State ─────────────────────────────────────────────
    private int quantity = 1;
    private long unitPrice = 0L;
    private boolean isFavorite = false;
    private String foodId = "";
    private String foodTitle = "";
    private String foodImageUrl = "";
    private String foodStoreId = "";
    private String storeName = "";
    private long storeDeliveryFee = 0L;
    private final NumberFormat currencyFormatter =
            NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        bindViews();
        receiveIntentData();
        setupQuantityControls();
        setupFavoriteButton();
        setupOrderButtons();
        setupMockReviews();
    }

    // ── ① Ánh xạ toàn bộ view ────────────────────────────
    private void bindViews() {
        imgFood             = findViewById(R.id.img_food_detail);
        ivFavoriteIcon      = findViewById(R.id.iv_favorite_icon);
        tvTitle             = findViewById(R.id.tv_food_detail_title);
        tvRating            = findViewById(R.id.tv_food_detail_rating);
        tvReviewCount       = findViewById(R.id.tv_food_detail_review_count);
        tvPrice             = findViewById(R.id.tv_food_detail_price);
        tvDescription       = findViewById(R.id.tv_food_detail_description);
        tvStoreName         = findViewById(R.id.tv_store_name);
        tvStoreDeliveryTime = findViewById(R.id.tv_store_delivery_time);
        tvFoodCategory      = findViewById(R.id.tv_food_category);
        tvPopularBadge      = (TextView) findViewById(R.id.tv_popular_badge);
        btnDecrease         = findViewById(R.id.btn_decrease);
        btnIncrease         = findViewById(R.id.btn_increase);
        tvQuantity          = findViewById(R.id.tv_quantity);
        btnOrder            = (TextView) findViewById(R.id.btn_order);
        btnBack             = findViewById(R.id.btn_back);
        btnFavorite         = findViewById(R.id.btn_favorite);
        btnAddToCart        = findViewById(R.id.btn_add_to_cart);
        rvReviews           = findViewById(R.id.rv_reviews);
    }

    // ── ② Nhận dữ liệu từ Intent extras ──────────────────
    private void receiveIntentData() {
        foodId                 = safe(getIntent().getStringExtra("foodId"));
        foodTitle              = safe(getIntent().getStringExtra("foodTitle"));
        String foodDescription = getIntent().getStringExtra("foodDescription");
        foodImageUrl           = safe(getIntent().getStringExtra("foodImageUrl"));
        float  foodRating      = getIntent().getFloatExtra("foodRating", 0f);
        unitPrice              = getIntent().getLongExtra("foodPrice", 0L);

        storeName              = safe(getIntent().getStringExtra("storeName"));
        String storeTime       = getIntent().getStringExtra("storeDeliveryTime");
        foodStoreId            = safe(getIntent().getStringExtra("foodStoreId"));
        storeDeliveryFee       = getIntent().getLongExtra("storeDeliveryFee", 0L);

        // Hiển thị dữ liệu cơ bản
        tvTitle.setText(foodTitle != null ? foodTitle : "Món ăn");
        tvDescription.setText(foodDescription != null ? foodDescription : "");
        tvRating.setText(String.format(Locale.US, "%.1f", foodRating));
        tvStoreName.setText(storeName != null ? storeName : "Nhà hàng");
        tvStoreDeliveryTime.setText(storeTime != null ? storeTime : "20-30 phút");

        // Giá
        tvPrice.setText(currencyFormatter.format(unitPrice) + "đ");

        // Load ảnh
        Glide.with(this)
                .load(foodImageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(imgFood);

        // Badge "Phổ biến" khi rating >= 4.5
        if (foodRating >= 4.5f) {
            tvPopularBadge.setVisibility(android.view.View.VISIBLE);
        }

        // Số đánh giá giả — mock data
        tvReviewCount.setText("(120 đánh giá)");

        // Phân loại — sẽ bind từ Intent sau khi có data thật
        tvFoodCategory.setText("Món chính");

        // Nút back
        btnBack.setOnClickListener(v -> finish());

        // Giá ban đầu trên nút Đặt
        updateOrderButtonText();
    }

    // ── ③ Điều khiển số lượng [−][n][+] ──────────────────
    private void setupQuantityControls() {
        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
                updateOrderButtonText();
            }
        });

        btnIncrease.setOnClickListener(v -> {
            quantity++;
            tvQuantity.setText(String.valueOf(quantity));
            updateOrderButtonText();
        });
    }

    /** Cập nhật label "Đặt · XYZđ" theo số lượng × đơn giá */
    private void updateOrderButtonText() {
        long total = unitPrice * quantity;
        btnOrder.setText("Đặt · " + currencyFormatter.format(total) + "đ");
    }

    // ── ④ Nút yêu thích (toggle UI only) ─────────────────
    private void setupFavoriteButton() {
        btnFavorite.setOnClickListener(v -> {
            isFavorite = !isFavorite;
            ivFavoriteIcon.setImageResource(
                    isFavorite ? android.R.drawable.btn_star_big_on
                               : android.R.drawable.btn_star_big_off);
            int tintColor = isFavorite ? R.color.home_badge_red : R.color.home_primary_orange;
            ivFavoriteIcon.setColorFilter(getColor(tintColor));
            Toast.makeText(this,
                    isFavorite ? "Đã thêm vào yêu thích" : "Đã bỏ yêu thích",
                    Toast.LENGTH_SHORT).show();
        });
    }

    // ── ⑤ Nút "Giỏ hàng" và "Đặt" ─────────────────────────
    private void setupOrderButtons() {
        btnAddToCart.setOnClickListener(v -> addToCart(quantity, false));
        btnOrder.setOnClickListener(v -> addToCart(quantity, true));
    }

    // ── ⑥ Mock reviews — chưa có Firestore collection ────
    private void setupMockReviews() {
        List<Review> reviews = new ArrayList<>();
        reviews.add(new Review("Nguyễn Văn A", "2 ngày trước", 5,
                "Món ăn rất ngon, phục vụ nhanh chóng. Sẽ ủng hộ thêm!", 12));
        reviews.add(new Review("Trần Thị B", "1 tuần trước", 4,
                "Phở ngon, nước dùng đậm đà. Hơi đông khách nhưng vẫn ngon.", 8));
        reviews.add(new Review("Lê Minh C", "2 tuần trước", 5,
                "Tuyệt vời! Thịt bò tươi, không bị dai. Rất đáng tiền.", 15));
        reviews.add(new Review("Phạm Thu D", "1 tháng trước", 4,
                "Vị ngon, giá cả hợp lý. Không gian sạch sẽ thoáng đãng.", 6));

        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setNestedScrollingEnabled(false);
        rvReviews.setAdapter(new ReviewAdapter(this, reviews));
    }

    private void addToCart(int qty, boolean openCartAfter) {
        Food food = new Food();
        food.setId(foodId);
        food.setTitle(foodTitle);
        food.setPrice(unitPrice);
        food.setImageUrl(foodImageUrl);
        food.setStoreId(foodStoreId);

        CartManager.AddResult result = CartManager.getInstance().addFood(
                food,
                qty,
                foodStoreId,
                storeName,
                storeDeliveryFee
        );
        if (result == CartManager.AddResult.STORE_MISMATCH) {
            Toast.makeText(this,
                    "Giỏ hàng chỉ chứa món từ 1 quán. Vui lòng xóa giỏ hiện tại trước.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
        if (openCartAfter) {
            Intent intent = new Intent(this, com.example.foodnow.MainActivity.class);
            intent.putExtra("open_tab", "cart");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }
    }

    private String safe(String value) {
        return value != null ? value : "";
    }
}
