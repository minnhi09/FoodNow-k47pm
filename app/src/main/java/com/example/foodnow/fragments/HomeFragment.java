package com.example.foodnow.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.activities.StoreDetailActivity;
import com.example.foodnow.R;
import com.example.foodnow.adapters.CategoryAdapter;
import com.example.foodnow.adapters.RecommendedFoodAdapter;
import com.example.foodnow.adapters.StoreAdapter;
import com.example.foodnow.models.Category;
import com.example.foodnow.models.RecommendedFood;
import com.example.foodnow.models.Store;
import com.example.foodnow.viewmodels.HomeViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    // ── Khai báo biến ────────────────────────────────────
    private HomeViewModel homeViewModel;

    private EditText etSearch;
    private TextView tvNoResults;
    private RecyclerView rvCategories;
    private RecyclerView rvStores;
    private RecyclerView rvRecommendedFoods;
    private ImageView ivHeaderCart;

    private CategoryAdapter categoryAdapter;
    private StoreAdapter storeAdapter;
    private RecommendedFoodAdapter recommendedFoodAdapter;

    // allStoreList: danh sách đầy đủ từ Firestore, không bao giờ bị xóa
    // storeList: danh sách đang hiển thị (có thể bị lọc theo từ khóa)
    private final List<Store> allStoreList = new ArrayList<>();
    private final List<Store> storeList = new ArrayList<>();

    private final List<Category> categoryList = new ArrayList<>();
    private final List<RecommendedFood> recommendedFoodList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ① Ánh xạ view
        etSearch        = view.findViewById(R.id.et_search);
        tvNoResults     = view.findViewById(R.id.tv_no_results);
        rvCategories    = view.findViewById(R.id.rv_categories);
        rvStores        = view.findViewById(R.id.rv_stores);
        rvRecommendedFoods = view.findViewById(R.id.rv_recommended_foods);
        ivHeaderCart    = view.findViewById(R.id.iv_header_cart);

        // ② Khởi tạo ViewModel
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // ③ Setup RecyclerViews + dữ liệu mock
        seedMockRecommendedFoods();
        setupCategoryRecyclerView();
        setupStoreRecyclerView();
        setupRecommendedRecyclerView();

        // ④ Setup search
        setupSearch();

        // ⑤ Observe data từ Firestore
        observeData();

        // ⑥ Action click nhanh
        ivHeaderCart.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Mở giỏ hàng", Toast.LENGTH_SHORT).show());
    }

    // ═══════════════════════════════════════════════════════
    // SEARCH
    // ═══════════════════════════════════════════════════════

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterStores(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Lọc danh sách quán ăn theo từ khóa.
     * Tìm kiếm trong: tên quán (name) + mô tả (description).
     * Nếu query rỗng → hiển thị tất cả.
     */
    private void filterStores(String query) {
        storeList.clear();

        if (query.isEmpty()) {
            storeList.addAll(allStoreList);
        } else {
            String lower = query.toLowerCase();
            for (Store store : allStoreList) {
                boolean nameMatch = store.getName() != null
                        && store.getName().toLowerCase().contains(lower);
                boolean descMatch = store.getDescription() != null
                        && store.getDescription().toLowerCase().contains(lower);
                if (nameMatch || descMatch) {
                    storeList.add(store);
                }
            }
        }

        storeAdapter.notifyDataSetChanged();
        tvNoResults.setVisibility(storeList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // ═══════════════════════════════════════════════════════
    // RECYCLERVIEW SETUP
    // ═══════════════════════════════════════════════════════

    private void setupCategoryRecyclerView() {
        categoryAdapter = new CategoryAdapter(
                requireContext(),
                categoryList,
                category -> {
                    // Xử lý click danh mục — tạm thời Toast để test
                    Toast.makeText(requireContext(),
                            "Danh mục: " + category.getName(),
                            Toast.LENGTH_SHORT).show();
                }
        );
        rvCategories.setLayoutManager(
                new LinearLayoutManager(requireContext(),
                        LinearLayoutManager.HORIZONTAL, false)
        );
        rvCategories.setAdapter(categoryAdapter);
    }

    private void setupStoreRecyclerView() {
        storeAdapter = new StoreAdapter(
                requireContext(),
                storeList,
                store -> {
                    // Mở màn hình chi tiết quán ăn
                    Intent intent = new Intent(requireContext(), StoreDetailActivity.class);
                    intent.putExtra("storeId", store.getId());
                    intent.putExtra("storeName", store.getName());
                    intent.putExtra("storeImage", store.getImageUrl());
                    intent.putExtra("storeRating", store.getRating());
                    intent.putExtra("storeDeliveryTime", store.getDeliveryTime());
                    intent.putExtra("storeDeliveryFee", store.getDeliveryFee());
                    startActivity(intent);
                }
        );
        rvStores.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvStores.setAdapter(storeAdapter);
    }

    private void setupRecommendedRecyclerView() {
        recommendedFoodAdapter = new RecommendedFoodAdapter(
                requireContext(),
                recommendedFoodList,
                food -> Toast.makeText(requireContext(),
                        "Đã thêm " + food.getName(),
                        Toast.LENGTH_SHORT).show()
        );
        rvRecommendedFoods.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvRecommendedFoods.setAdapter(recommendedFoodAdapter);
    }

    // ═══════════════════════════════════════════════════════
    // OBSERVE DATA
    // ═══════════════════════════════════════════════════════

    private void observeData() {
        // Observe danh mục
        homeViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            categoryList.clear();
            categoryList.add(new Category("all", "Tất cả", ""));
            if (categories != null && !categories.isEmpty()) {
                categoryList.addAll(categories);
            } else {
                categoryList.add(new Category("pho", "Phở", ""));
                categoryList.add(new Category("pizza", "Pizza", ""));
                categoryList.add(new Category("dessert", "Tráng miệng", ""));
            }
            categoryAdapter.notifyDataSetChanged();
        });

        // Observe quán ăn — lưu vào allStoreList, rồi áp filter hiện tại
        homeViewModel.getStores().observe(getViewLifecycleOwner(), stores -> {
            allStoreList.clear();
            if (stores != null && !stores.isEmpty()) {
                allStoreList.addAll(stores);
            } else {
                addMockStores();
            }
            // Áp lại từ khóa đang nhập (nếu có)
            filterStores(etSearch.getText().toString().trim());
        });
    }

    // ═══════════════════════════════════════════════════════
    // DỮ LIỆU MẪU (dùng khi Firestore trống)
    // ═══════════════════════════════════════════════════════

    private void seedMockRecommendedFoods() {
        recommendedFoodList.clear();
        recommendedFoodList.add(new RecommendedFood(
                "Phở Bò Đặc Biệt",
                "Phở Hà Nội",
                65000,
                4.8f,
                "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43",
                true
        ));
        recommendedFoodList.add(new RecommendedFood(
                "Gỏi Cuốn Tôm Thịt",
                "Quán Việt",
                35000,
                4.7f,
                "https://images.unsplash.com/photo-1604908176997-431ec29b605e",
                false
        ));
        recommendedFoodList.add(new RecommendedFood(
                "Cơm Gà Xối Mỡ",
                "Cơm Gà Hải Nam",
                45000,
                4.6f,
                "https://images.unsplash.com/photo-1512058564366-18510be2db19",
                true
        ));
        recommendedFoodList.add(new RecommendedFood(
                "Xiên Nướng BBQ",
                "BBQ House",
                25000,
                4.5f,
                "https://images.unsplash.com/photo-1529692236671-f1dc3ce964f1",
                false
        ));
    }

    private void addMockStores() {
        allStoreList.add(buildStore("Phở Hà Nội", "Việt Nam", 4.8f, "20-30 phút",
                "https://images.unsplash.com/photo-1544025162-d76694265947"));
        allStoreList.add(buildStore("Bánh Mì Sài Gòn", "Việt Nam", 4.6f, "15-25 phút",
                "https://images.unsplash.com/photo-1481070414801-51fd732d7184"));
        allStoreList.add(buildStore("Trà Sữa Đài Loan", "Đồ uống", 4.9f, "10-20 phút",
                "https://images.unsplash.com/photo-1558857563-c0c74f00b5f1"));
    }

    private Store buildStore(String name, String address, float rating, String time, String imageUrl) {
        Store store = new Store();
        store.setName(name);
        store.setAddress(address);
        store.setRating(rating);
        store.setDeliveryTime(time);
        store.setImageUrl(imageUrl);
        return store;
    }
}
