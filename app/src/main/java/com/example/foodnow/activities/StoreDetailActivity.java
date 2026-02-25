package com.example.foodnow.activities;

import android.app.AlertDialog;
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
import com.example.foodnow.models.CartItem;
import com.example.foodnow.models.Food;
import com.example.foodnow.utils.CartManager;
import com.example.foodnow.viewmodels.StoreDetailViewModel;

import java.util.ArrayList;
import java.util.List;

public class StoreDetailActivity extends AppCompatActivity {

    public static final String EXTRA_STORE_ID   = "store_id";
    public static final String EXTRA_STORE_NAME = "store_name";
    public static final String EXTRA_STORE_DESC = "store_desc";
    public static final String EXTRA_STORE_IMG  = "store_img";
    public static final String EXTRA_STORE_RATING = "store_rating";
    public static final String EXTRA_STORE_TIME   = "store_time";

    private RecyclerView rvFoods;
    private FoodAdapter foodAdapter;
    private List<Food> foodList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_detail);

        // Lấy dữ liệu quán từ Intent
        String storeId   = getIntent().getStringExtra(EXTRA_STORE_ID);
        String storeName = getIntent().getStringExtra(EXTRA_STORE_NAME);
        String storeDesc = getIntent().getStringExtra(EXTRA_STORE_DESC);
        String storeImg  = getIntent().getStringExtra(EXTRA_STORE_IMG);
        float storeRating = getIntent().getFloatExtra(EXTRA_STORE_RATING, 0f);
        String storeTime = getIntent().getStringExtra(EXTRA_STORE_TIME);

        // Ánh xạ header
        TextView tvName   = findViewById(R.id.tv_store_name);
        TextView tvDesc   = findViewById(R.id.tv_store_description);
        TextView tvRating = findViewById(R.id.tv_store_rating);
        TextView tvTime   = findViewById(R.id.tv_store_time);
        ImageView imgStore = findViewById(R.id.img_store_detail);

        tvName.setText(storeName);
        tvDesc.setText(storeDesc);
        tvRating.setText("⭐ " + storeRating);
        tvTime.setText(storeTime);

        if (storeImg != null && !storeImg.isEmpty()) {
            Glide.with(this).load(storeImg).placeholder(R.mipmap.ic_launcher).into(imgStore);
        }

        // Setup RecyclerView món ăn
        rvFoods = findViewById(R.id.rv_foods);
        foodAdapter = new FoodAdapter(this, foodList, food -> {
            addToCart(food, storeId, storeName);
        });
        rvFoods.setLayoutManager(new LinearLayoutManager(this));
        rvFoods.setAdapter(foodAdapter);

        // Load danh sách món từ Firestore
        StoreDetailViewModel viewModel = new ViewModelProvider(this).get(StoreDetailViewModel.class);
        viewModel.getFoods(storeId).observe(this, foods -> {
            foodList.clear();
            foodList.addAll(foods);
            foodAdapter.notifyDataSetChanged();
        });
    }

    /** Thêm món vào giỏ — hỏi xác nhận nếu đổi quán */
    private void addToCart(Food food, String storeId, String storeName) {
        CartManager cart = CartManager.getInstance();

        if (cart.isFromDifferentStore(storeId)) {
            new AlertDialog.Builder(this)
                    .setTitle("Đổi quán?")
                    .setMessage("Giỏ hàng đang có món từ quán \"" + cart.getCurrentStoreName()
                            + "\". Bạn có muốn xóa giỏ cũ và thêm món từ quán \"" + storeName + "\"?")
                    .setPositiveButton("Đồng ý", (d, w) -> {
                        cart.clearCart();
                        doAddToCart(food, storeId, storeName);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        } else {
            doAddToCart(food, storeId, storeName);
        }
    }

    private void doAddToCart(Food food, String storeId, String storeName) {
        CartItem cartItem = new CartItem(
                food.getId(), food.getTitle(), food.getPrice(),
                1, food.getImageUrl(), storeId, storeName);
        CartManager.getInstance().addItem(cartItem);
        Toast.makeText(this, "Đã thêm \"" + food.getTitle() + "\" vào giỏ", Toast.LENGTH_SHORT).show();
    }
}
