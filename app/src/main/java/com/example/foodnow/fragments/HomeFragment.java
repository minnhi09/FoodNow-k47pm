package com.example.foodnow.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.R;
import com.example.foodnow.adapters.CategoryAdapter;
import com.example.foodnow.adapters.StoreAdapter;
import com.example.foodnow.models.Category;
import com.example.foodnow.models.Store;
import com.example.foodnow.viewmodels.HomeViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    // ── Khai báo biến ────────────────────────────────────
    private HomeViewModel homeViewModel;

    private RecyclerView rvCategories, rvStores;

    private CategoryAdapter categoryAdapter;
    private StoreAdapter storeAdapter;

    private List<Category> categoryList = new ArrayList<>();
    private List<Store> storeList = new ArrayList<>();

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
        rvCategories = view.findViewById(R.id.rv_categories);
        rvStores     = view.findViewById(R.id.rv_stores);

        // ② Khởi tạo ViewModel
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // ③ Setup RecyclerView danh mục (ngang)
        setupCategoryRecyclerView();

        // ④ Setup RecyclerView quán ăn (dọc)
        setupStoreRecyclerView();

        // ⑤ Observe data từ ViewModel
        observeData();
    }

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
                    // Xử lý click quán ăn — tạm thời Toast để test
                    Toast.makeText(requireContext(),
                            "Quán: " + store.getName(),
                            Toast.LENGTH_SHORT).show();
                }
        );
        rvStores.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );
        rvStores.setAdapter(storeAdapter);
    }

    private void observeData() {
        // Observe danh mục
        homeViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            categoryList.clear();
            categoryList.addAll(categories);
            categoryAdapter.notifyDataSetChanged();
        });

        // Observe quán ăn
        homeViewModel.getStores().observe(getViewLifecycleOwner(), stores -> {
            storeList.clear();
            storeList.addAll(stores);
            storeAdapter.notifyDataSetChanged();
        });
    }
}