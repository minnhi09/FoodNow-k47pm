package com.example.foodnow.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.R;
import com.example.foodnow.activities.AddEditFoodActivity;
import com.example.foodnow.activities.StoreOwnerActivity;
import com.example.foodnow.adapters.ManageFoodAdapter;
import com.example.foodnow.models.Food;
import com.example.foodnow.repositories.FoodRepository;
import com.example.foodnow.viewmodels.StoreOwnerViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tab "Thực đơn" — danh sách món ăn + CRUD + search + filter danh mục.
 */
public class ManageFoodsFragment extends Fragment {

    private StoreOwnerViewModel viewModel;
    private ManageFoodAdapter   adapter;
    private String              storeId;

    // State
    private String selectedCategoryId = null;  // null = tất cả

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owner_manage_foods, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        storeId   = ((StoreOwnerActivity) requireActivity()).getStoreId();
        viewModel = new ViewModelProvider(requireActivity()).get(StoreOwnerViewModel.class);

        RecyclerView     rv          = view.findViewById(R.id.rv_manage_foods);
        LinearLayout     emptyView   = view.findViewById(R.id.layout_empty);
        TextView         tvCount     = view.findViewById(R.id.tv_food_count);
        EditText         etSearch    = view.findViewById(R.id.et_search_food);
        LinearLayout     chipGroup   = view.findViewById(R.id.chip_group_categories);
        TextView         chipAll     = view.findViewById(R.id.chip_all);
        FloatingActionButton fab     = view.findViewById(R.id.fab_add_food);

        // Setup adapter
        adapter = new ManageFoodAdapter(
                requireContext(),
                new ArrayList<>(),
                // Sửa
                food -> {
                    Intent intent = new Intent(requireContext(), AddEditFoodActivity.class);
                    intent.putExtra("storeId",          storeId);
                    intent.putExtra("foodId",            food.getId());
                    intent.putExtra("foodTitle",         food.getTitle());
                    intent.putExtra("foodDescription",   food.getDescription());
                    intent.putExtra("foodPrice",         food.getPrice());
                    intent.putExtra("foodImageUrl",      food.getImageUrl());
                    intent.putExtra("foodCategoryId",    food.getCategoryId());
                    intent.putExtra("foodAvailable",     food.isAvailable());
                    startActivity(intent);
                },
                // Xóa
                food -> new AlertDialog.Builder(requireContext())
                        .setTitle("Xóa món ăn")
                        .setMessage("Bạn có chắc muốn xóa \"" + food.getTitle() + "\"?")
                        .setPositiveButton("Xóa", (d, w) -> viewModel.deleteFood(food.getId()))
                        .setNegativeButton("Hủy", null)
                        .show(),
                // Toggle ẩn/hiện
                (food, isAvailable) -> new FoodRepository().updateFoodAvailability(food.getId(), isAvailable)
        );

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        // Search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                adapter.setSearchQuery(s.toString());
                tvCount.setText(adapter.getFilteredCount() + " món");
            }
        });

        // "Tất cả" chip
        chipAll.setOnClickListener(v -> {
            selectedCategoryId = null;
            adapter.setCategoryFilter(null);
            tvCount.setText(adapter.getFilteredCount() + " món");
        });

        // Observe foods
        viewModel.getFoods(storeId).observe(getViewLifecycleOwner(), foods -> {
            if (foods == null) foods = new ArrayList<>();
            adapter.setFoods(foods);
            tvCount.setText(adapter.getFilteredCount() + " món");

            boolean isEmpty = foods.isEmpty();
            emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            rv.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

            // Xây dựng chip danh mục từ dữ liệu thực tế
            buildCategoryChips(chipGroup, chipAll, foods, tvCount);
        });

        // Thông báo thao tác
        viewModel.getActionMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });

        // FAB → thêm mới
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddEditFoodActivity.class);
            intent.putExtra("storeId", storeId);
            startActivity(intent);
        });
    }

    /** Tạo chip danh mục động dựa trên categoryId xuất hiện trong foods. */
    private void buildCategoryChips(LinearLayout chipGroup, TextView chipAll,
                                    List<Food> foods, TextView tvCount) {
        // Xóa chips cũ (giữ lại chipAll ở index 0)
        while (chipGroup.getChildCount() > 1) {
            chipGroup.removeViewAt(1);
        }

        // Tập hợp các categoryId duy nhất (giữ thứ tự xuất hiện)
        Map<String, String> categoryMap = new LinkedHashMap<>();
        for (Food f : foods) {
            if (f.getCategoryId() != null && !f.getCategoryId().isEmpty()) {
                // Key: categoryId, Value: dùng categoryId làm nhãn tạm (có thể load tên sau)
                categoryMap.put(f.getCategoryId(), f.getCategoryId());
            }
        }

        for (String catId : categoryMap.keySet()) {
            TextView chip = new TextView(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    (int) (32 * getResources().getDisplayMetrics().density));
            params.setMarginEnd((int) (8 * getResources().getDisplayMetrics().density));
            chip.setLayoutParams(params);
            chip.setPadding(
                    (int)(14 * getResources().getDisplayMetrics().density), 0,
                    (int)(14 * getResources().getDisplayMetrics().density), 0);
            chip.setGravity(android.view.Gravity.CENTER);
            chip.setText(catId);
            chip.setTextSize(12);
            setChipStyle(chip, false);

            chip.setOnClickListener(v -> {
                selectedCategoryId = catId;
                adapter.setCategoryFilter(catId);
                tvCount.setText(adapter.getFilteredCount() + " món");
                // Reset all chips
                setChipStyle(chipAll, false);
                chipAll.setTextColor(0xFFFF6B35);
                for (int i = 1; i < chipGroup.getChildCount(); i++) {
                    setChipStyle((TextView) chipGroup.getChildAt(i), false);
                }
                setChipStyle(chip, true);
            });

            chipGroup.addView(chip);
        }

        // "Tất cả" always resets styles
        chipAll.setOnClickListener(v -> {
            selectedCategoryId = null;
            adapter.setCategoryFilter(null);
            tvCount.setText(adapter.getFilteredCount() + " món");
            setChipStyle(chipAll, true);
            for (int i = 1; i < chipGroup.getChildCount(); i++) {
                setChipStyle((TextView) chipGroup.getChildAt(i), false);
            }
        });
    }

    private void setChipStyle(TextView chip, boolean selected) {
        if (selected) {
            chip.setBackgroundResource(R.drawable.bg_tab_selected);
            chip.setTextColor(0xFFFFFFFF);
        } else {
            chip.setBackgroundResource(R.drawable.bg_tab_unselected);
            chip.setTextColor(0xFFFF6B35);
        }
    }
}
