package com.example.foodnow.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.foodnow.viewmodels.StoreOwnerViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

/**
 * Tab "Thực đơn" — danh sách món ăn + CRUD.
 * FAB "+" → AddEditFoodActivity (thêm mới).
 * Nút "Sửa" → AddEditFoodActivity với foodId (chỉnh sửa).
 * Nút "Xóa" → AlertDialog xác nhận → deleteFood.
 */
public class ManageFoodsFragment extends Fragment {

    private StoreOwnerViewModel viewModel;
    private ManageFoodAdapter adapter;
    private final ArrayList<com.example.foodnow.models.Food> foodList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_foods, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String storeId = ((StoreOwnerActivity) requireActivity()).getStoreId();
        viewModel = new ViewModelProvider(requireActivity()).get(StoreOwnerViewModel.class);

        RecyclerView rv       = view.findViewById(R.id.rv_manage_foods);
        LinearLayout emptyView = view.findViewById(R.id.layout_empty);
        TextView tvCount      = view.findViewById(R.id.tv_food_count);
        FloatingActionButton fab = view.findViewById(R.id.fab_add_food);

        // Setup RecyclerView
        adapter = new ManageFoodAdapter(requireContext(), foodList,
                // Sửa → mở AddEditFoodActivity với food đã chọn
                food -> {
                    Intent intent = new Intent(requireContext(), AddEditFoodActivity.class);
                    intent.putExtra("storeId", storeId);
                    intent.putExtra("foodId",  food.getId());
                    intent.putExtra("foodTitle",       food.getTitle());
                    intent.putExtra("foodDescription", food.getDescription());
                    intent.putExtra("foodPrice",       food.getPrice());
                    intent.putExtra("foodImageUrl",    food.getImageUrl());
                    intent.putExtra("foodCategoryId",  food.getCategoryId());
                    intent.putExtra("foodAvailable",   food.isAvailable());
                    startActivity(intent);
                },
                // Xóa → xác nhận trước
                food -> new AlertDialog.Builder(requireContext())
                        .setTitle("Xóa món ăn")
                        .setMessage("Bạn có chắc muốn xóa \"" + food.getTitle() + "\"?")
                        .setPositiveButton("Xóa", (d, w) -> viewModel.deleteFood(food.getId()))
                        .setNegativeButton("Hủy", null)
                        .show()
        );

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        // Quan sát danh sách món ăn
        viewModel.getFoods(storeId).observe(getViewLifecycleOwner(), foods -> {
            foodList.clear();
            if (foods != null) foodList.addAll(foods);
            adapter.notifyDataSetChanged();

            boolean isEmpty = foodList.isEmpty();
            emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            rv.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            tvCount.setText(foodList.size() + " món");
        });

        // Quan sát thông báo thao tác
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
}

