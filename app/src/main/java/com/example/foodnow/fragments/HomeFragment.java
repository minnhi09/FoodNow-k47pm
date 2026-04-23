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
import android.widget.LinearLayout;
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
import com.example.foodnow.models.Order;
import com.example.foodnow.models.RecommendedFood;
import com.example.foodnow.models.Store;
import com.example.foodnow.repositories.OrderRepository;
import com.example.foodnow.utils.CartManager;
import com.example.foodnow.viewmodels.HomeViewModel;
import com.example.foodnow.viewmodels.ProfileViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    // ── Khai báo biến ────────────────────────────────────
    private HomeViewModel homeViewModel;

    private TextView tvDeliveryAddress;
    private LinearLayout layoutOrderCard;
    private TextView tvOrderStatus;
    private TextView tvCartBadge;
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
    // storeList: danh sách đang hiển thị (có thể bị lọc theo từ khóa + danh mục)
    private final List<Store> allStoreList = new ArrayList<>();
    private final List<Store> storeList = new ArrayList<>();

    // ID danh mục đang chọn; "" = "Tất cả" (không lọc)
    private String selectedCategoryId = "";

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
        tvDeliveryAddress = view.findViewById(R.id.tv_delivery_address);
        layoutOrderCard   = view.findViewById(R.id.layout_order_card);
        tvOrderStatus     = view.findViewById(R.id.tv_order_status);
        tvCartBadge       = view.findViewById(R.id.tv_header_cart_badge);
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

        // ⑥ Observe địa chỉ giao hàng từ profile user
        observeUserAddress();

        // ⑦ Observe đơn hàng đang hoạt động
        observeActiveOrder();

        // ⑧ Action click nhanh
        ivHeaderCart.setOnClickListener(v -> {
            android.view.View navView = requireActivity().findViewById(R.id.bottom_navigation);
            if (navView instanceof com.google.android.material.bottomnavigation.BottomNavigationView) {
                ((com.google.android.material.bottomnavigation.BottomNavigationView) navView)
                        .setSelectedItemId(R.id.nav_cart);
            }
        });
        layoutOrderCard.setOnClickListener(v -> {
            android.view.View navView = requireActivity().findViewById(R.id.bottom_navigation);
            if (navView instanceof com.google.android.material.bottomnavigation.BottomNavigationView) {
                ((com.google.android.material.bottomnavigation.BottomNavigationView) navView)
                        .setSelectedItemId(R.id.nav_orders);
            }
        });

        updateHeaderCartBadge();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateHeaderCartBadge();
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
     * Lọc danh sách quán ăn theo TỪ KHÓA và DANH MỤC (kết hợp cùng lúc).
     * - query rỗng + categoryId rỗng → hiển thị tất cả
     * - Chỉ query → lọc theo tên/mô tả
     * - Chỉ category → lọc theo categoryId của store
     * - Cả hai → phải thỏa mãn cả hai điều kiện (AND)
     */
    private void filterStores(String query) {
        storeList.clear();
        String lower = query.toLowerCase();

        for (Store store : allStoreList) {
            // Điều kiện 1: lọc theo danh mục
            boolean categoryMatch = selectedCategoryId.isEmpty()
                    || selectedCategoryId.equals(store.getCategoryId());

            // Điều kiện 2: lọc theo từ khóa tìm kiếm
            boolean searchMatch = query.isEmpty()
                    || (store.getName() != null && store.getName().toLowerCase().contains(lower))
                    || (store.getDescription() != null && store.getDescription().toLowerCase().contains(lower));

            if (categoryMatch && searchMatch) {
                storeList.add(store);
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
                    String clickedId = category.getId();
                    // Click "Tất cả" HOẶC click lại chip đang chọn → bỏ lọc
                    if ("all".equals(clickedId) || clickedId.equals(selectedCategoryId)) {
                        selectedCategoryId = "";
                    } else {
                        selectedCategoryId = clickedId;
                    }
                    // Cập nhật visual trên chip
                    categoryAdapter.setSelectedCategory(selectedCategoryId);
                    // Áp lại filter (giữ nguyên từ khóa search hiện tại)
                    filterStores(etSearch.getText().toString().trim());
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
                "https://images.unsplash.com/photo-1544025162-d76694265947", "pho"));
        allStoreList.add(buildStore("Bánh Mì Sài Gòn", "Việt Nam", 4.6f, "15-25 phút",
                "https://images.unsplash.com/photo-1481070414801-51fd732d7184", "banh-mi"));
        allStoreList.add(buildStore("Trà Sữa Đài Loan", "Đồ uống", 4.9f, "10-20 phút",
                "https://images.unsplash.com/photo-1558857563-c0c74f00b5f1", "dessert"));
    }

    private Store buildStore(String name, String address, float rating, String time, String imageUrl, String categoryId) {
        Store store = new Store();
        store.setName(name);
        store.setAddress(address);
        store.setRating(rating);
        store.setDeliveryTime(time);
        store.setImageUrl(imageUrl);
        store.setCategoryId(categoryId);
        return store;
    }

    // ═══════════════════════════════════════════════════════
    // ĐỊA CHỈ GIAO HÀNG — lấy từ profile user
    // ═══════════════════════════════════════════════════════

    private void observeUserAddress() {
        ProfileViewModel profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);
        profileViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null && user.getAddress() != null && !user.getAddress().isEmpty()) {
                tvDeliveryAddress.setText(user.getAddress());
            } else {
                tvDeliveryAddress.setText("Chưa thiết lập địa chỉ");
            }
        });
    }

    // ═══════════════════════════════════════════════════════
    // ĐƠN HÀNG ĐANG HOẠT ĐỘNG — chỉ hiển thị khi có đơn chưa hoàn thành
    // ═══════════════════════════════════════════════════════

    private void observeActiveOrder() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) return;

        OrderRepository orderRepo = new OrderRepository();
        orderRepo.getOrdersByUser(uid).observe(getViewLifecycleOwner(), orders -> {
            if (orders == null || orders.isEmpty()) {
                layoutOrderCard.setVisibility(View.GONE);
                return;
            }
            // Tìm đơn hàng đang hoạt động (chưa hoàn thành và chưa hủy)
            // orders được sắp xếp theo createdAt giảm dần → lấy đơn mới nhất trước
            Order activeOrder = null;
            for (Order order : orders) {
                String status = order.getStatus();
                if (!Order.STATUS_DONE.equals(status) && !Order.STATUS_CANCELLED.equals(status)) {
                    activeOrder = order;
                    break;
                }
            }
            if (activeOrder != null) {
                tvOrderStatus.setText(activeOrder.getStatus());
                layoutOrderCard.setVisibility(View.VISIBLE);
            } else {
                layoutOrderCard.setVisibility(View.GONE);
            }
        });
    }

    private void updateHeaderCartBadge() {
        int count = CartManager.getInstance().getItemCount();
        if (count <= 0) {
            tvCartBadge.setVisibility(View.GONE);
            return;
        }
        tvCartBadge.setVisibility(View.VISIBLE);
        tvCartBadge.setText(String.valueOf(Math.min(count, 99)));
    }
}
