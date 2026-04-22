package com.example.foodnow.activities;

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
import com.example.foodnow.viewmodels.StoreDetailViewModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StoreDetailActivity extends AppCompatActivity {

    // ── Khai báo biến ────────────────────────────────────
    private StoreDetailViewModel viewModel;

    private ImageView imgStore;
    private ImageView btnBack;
    private TextView tvName, tvRating, tvTime, tvFee;
    private RecyclerView rvFoods;

    private FoodAdapter foodAdapter;
    private final List<Food> foodList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_detail);

        // ① Ánh xạ view
        imgStore = findViewById(R.id.img_store_detail);
        btnBack = findViewById(R.id.btn_back);
        tvName = findViewById(R.id.tv_store_detail_name);
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

        // ⑤ Setup RecyclerView danh sách món
        foodAdapter = new FoodAdapter(this, foodList, food -> {
            // TODO: Kết nối CartManager khi TV3 hoàn thành
            Toast.makeText(this,
                    "Đã thêm: " + food.getTitle(),
                    Toast.LENGTH_SHORT).show();
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

    // Định dạng phí giao hàng
    private String formatDeliveryFee(long fee) {
        if (fee <= 0) return "Miễn phí ship";
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        return "Ship " + nf.format(fee) + "đ";
    }
}
