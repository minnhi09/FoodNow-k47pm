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
import com.example.foodnow.adapters.ReviewAdapter;
import com.example.foodnow.models.CartItem;
import com.example.foodnow.models.Review;
import com.example.foodnow.utils.CartManager;
import com.example.foodnow.viewmodels.FavoritesViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
    private String currentFavoriteId = "";
    private String foodId = "";
    private String foodTitle = "";
    private String foodImageUrl = "";
    private String storeId = "";
    private String storeName = "";

    private FavoritesViewModel favoritesViewModel;

    private final NumberFormat currencyFormatter =
            NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        bindViews();
        receiveIntentData();
        setupFavoritesObservers();
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
        foodId                 = safeValue(getIntent().getStringExtra("foodId"));
        foodTitle              = safeValue(getIntent().getStringExtra("foodTitle"));
        String foodDescription = getIntent().getStringExtra("foodDescription");
        foodImageUrl           = safeValue(getIntent().getStringExtra("foodImageUrl"));
        float  foodRating      = getIntent().getFloatExtra("foodRating", 0f);
        unitPrice              = getIntent().getLongExtra("foodPrice", 0L);

        storeId                = safeValue(getIntent().getStringExtra("storeId"));
        storeName              = safeValue(getIntent().getStringExtra("storeName"));
        String storeTime       = getIntent().getStringExtra("storeDeliveryTime");

        // Hiển thị dữ liệu cơ bản
        tvTitle.setText(foodTitle.isEmpty() ? "Món ăn" : foodTitle);
        tvDescription.setText(foodDescription != null ? foodDescription : "");
        tvRating.setText(String.format(Locale.US, "%.1f", foodRating));
        tvStoreName.setText(storeName.isEmpty() ? "Nhà hàng" : storeName);
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

    private void setupFavoritesObservers() {
        favoritesViewModel = new ViewModelProvider(this).get(FavoritesViewModel.class);

        favoritesViewModel.getItemFavoritedLiveData().observe(this, favorited -> {
            isFavorite = Boolean.TRUE.equals(favorited);
            applyFavoriteUi();
        });

        favoritesViewModel.getCurrentFavoriteIdLiveData().observe(this, favoriteId -> {
            currentFavoriteId = favoriteId == null ? "" : favoriteId;
        });

        String userId = getCurrentUserId();
        if (!userId.isEmpty() && !foodId.isEmpty()) {
            favoritesViewModel.checkFavorite(userId, foodId, "food");
        } else {
            applyFavoriteUi();
        }
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

    // ── ④ Nút yêu thích ──────────────────────────────────
    private void setupFavoriteButton() {
        btnFavorite.setOnClickListener(v -> {
            String userId = getCurrentUserId();
            if (userId.isEmpty()) {
                Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isFavorite) {
                if (!currentFavoriteId.isEmpty()) {
                    favoritesViewModel.removeFavorite(currentFavoriteId);
                    Toast.makeText(this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                }
            } else {
                favoritesViewModel.addFavorite(userId, "food", foodId, foodTitle, foodImageUrl);
                Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── ⑤ Nút "Giỏ hàng" và "Đặt" ───────────────────────
    private void setupOrderButtons() {
        btnAddToCart.setOnClickListener(v -> addCurrentFoodToCart(quantity, null));

        btnOrder.setOnClickListener(v -> 
                addCurrentFoodToCart(quantity, () -> 
                        startActivity(new Intent(this, CheckoutActivity.class))));
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

    private void addCurrentFoodToCart(int addQuantity, Runnable onAdded) {
        if (foodId.isEmpty() || storeId.isEmpty()) {
            Toast.makeText(this, "Thông tin cửa hàng không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        CartManager cartManager = CartManager.getInstance();
        
        CartItem cartItem = new CartItem(
                foodId,
                foodTitle,
                unitPrice,
                1,
                foodImageUrl,
                storeId,
                storeName
        );

        Runnable addAction = () -> {
            for (int i = 0; i < addQuantity; i++) {
                cartManager.addItem(cartItem);
            }
            Toast.makeText(this, "Đã thêm " + foodTitle + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
            if (onAdded != null) {
                onAdded.run();
            }
        };

        if (cartManager.isFromDifferentStore(storeId)) {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận thay đổi")
                    .setMessage("Bạn đang chọn món ở cửa hàng khác. Bạn có muốn xóa giỏ hàng hiện tại không?")
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .setPositiveButton("Đồng ý", (dialog, which) -> {
                        cartManager.clearCart();
                        addAction.run();
                    })
                    .show();
            return;
        }

        addAction.run();
    }

    private void applyFavoriteUi() {
        ivFavoriteIcon.setImageResource(isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        // Không cần setColorFilter vì icon đã có màu trong vector
        ivFavoriteIcon.setColorFilter(null);
    }

    private String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser == null ? "" : currentUser.getUid();
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }
}
